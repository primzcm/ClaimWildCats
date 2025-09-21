package com.claimwildcats.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.claimwildcats.api.config.FirebaseProperties;
import com.claimwildcats.api.domain.CampusZone;
import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.dto.CreateLostItemRequest;
import com.claimwildcats.api.dto.ItemSearchResponse;
import com.claimwildcats.api.dto.UpdateItemStatusRequest;
import com.google.api.core.SettableApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ItemServiceTest {

    private static final String BUCKET = "claimwildcats-dev.appspot.com";

    private final FirebaseFacade firebaseFacade = mock(FirebaseFacade.class);
    private final Firestore firestore = mock(Firestore.class);
    private final CollectionReference collection = mock(CollectionReference.class);
    private final DocumentReference document = mock(DocumentReference.class);
    private final FirebaseProperties firebaseProperties = new FirebaseProperties();

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        firebaseProperties.setStorageBucket(BUCKET);
        itemService = new ItemService(firebaseFacade, firebaseProperties);
        lenient().when(firebaseFacade.getFirestore()).thenReturn(Optional.of(firestore));
        lenient().when(firestore.collection("items")).thenReturn(collection);
        lenient().when(collection.document()).thenReturn(document);
        lenient().when(collection.document(org.mockito.ArgumentMatchers.anyString())).thenReturn(document);
        lenient().when(document.getId()).thenReturn("doc-1");
    }

    @Test
    void createLostItem_persistsExpectedFields() throws Exception {
        CreateLostItemRequest request = new CreateLostItemRequest(
                "Blue Backpack",
                "Canvas bag with laptop",
                "Library Atrium",
                CampusZone.LIBRARY,
                Instant.parse("2024-03-01T10:15:30Z"),
                List.of("backpack", "laptop"),
                List.of("gs://" + BUCKET + "/items/doc-1/evidence.pdf"));

        prepareFirestoreResult(Map.of(
                "title", "Blue Backpack",
                "description", "Canvas bag with laptop",
                "locationText", "Library Atrium",
                "status", "LOST",
                "campusZone", "Library",
                "docUrls", List.of("gs://" + BUCKET + "/items/doc-1/evidence.pdf"),
                "tags", List.of("backpack", "laptop"),
                "createdAt", Timestamp.now(),
                "reporterId", "user-1"));

        ItemDetail detail = itemService.createLostItem(request, "user-1");

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        org.mockito.Mockito.verify(document).set(captor.capture());
        Map<String, Object> stored = captor.getValue();

        assertThat(stored)
                .containsEntry("title", "Blue Backpack")
                .containsEntry("status", "LOST")
                .containsEntry("campusZone", "Library")
                .containsEntry("reporterId", "user-1")
                .containsEntry("docUrls", List.of("gs://" + BUCKET + "/items/doc-1/evidence.pdf"));

        assertThat(detail.id()).isEqualTo("doc-1");
        assertThat(detail.docUrls()).containsExactly("gs://" + BUCKET + "/items/doc-1/evidence.pdf");
    }

    @Test
    void createLostItem_rejectsDocUrlFromOtherBucket() {
        CreateLostItemRequest request = new CreateLostItemRequest(
                "Blue Backpack",
                "Canvas bag with laptop",
                "Library Atrium",
                CampusZone.LIBRARY,
                Instant.parse("2024-03-01T10:15:30Z"),
                List.of("backpack", "laptop"),
                List.of("gs://someone-else/items/doc-1/evidence.pdf"));

        assertThrows(IllegalArgumentException.class, () -> itemService.createLostItem(request, "user-1"));
    }

    @Test
    void createLostItem_rejectsNonPdfDocUrl() {
        CreateLostItemRequest request = new CreateLostItemRequest(
                "Blue Backpack",
                "Canvas bag with laptop",
                "Library Atrium",
                CampusZone.LIBRARY,
                Instant.parse("2024-03-01T10:15:30Z"),
                List.of("backpack", "laptop"),
                List.of("gs://" + BUCKET + "/items/doc-1/evidence.png"));

        assertThrows(IllegalArgumentException.class, () -> itemService.createLostItem(request, "user-1"));
    }

    @Test
    void updateStatus_requiresOwnership() throws Exception {
        prepareFirestoreResult(Map.of(
                "title", "Blue Backpack",
                "description", "Canvas bag with laptop",
                "locationText", "Library Atrium",
                "status", "LOST",
                "campusZone", "Library",
                "docUrls", List.of(),
                "tags", List.of(),
                "createdAt", Timestamp.now(),
                "reporterId", "owner-1"));

        assertThrows(
                org.springframework.security.access.AccessDeniedException.class,
                () -> itemService.updateStatus("doc-1", new UpdateItemStatusRequest(ItemStatus.CLAIMED, null), "other-user"));
    }

    @Test
    void searchItems_fallsBackWhenNoFirestore() {
        when(firebaseFacade.getFirestore()).thenReturn(Optional.empty());
        ItemSearchResponse response = itemService.searchItems(null, null, null, 0, 10);
        assertThat(response.items()).isNotEmpty();
    }

    private void prepareFirestoreResult(Map<String, Object> data) throws Exception {
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        SettableApiFuture<WriteResult> writeFuture = SettableApiFuture.create();
        writeFuture.set(mock(WriteResult.class));
        when(document.set(mapCaptor.capture())).thenReturn(writeFuture);

        SettableApiFuture<WriteResult> mergeFuture = SettableApiFuture.create();
        mergeFuture.set(mock(WriteResult.class));
        when(document.set(any(), org.mockito.ArgumentMatchers.eq(SetOptions.merge()))).thenReturn(mergeFuture);

        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.getId()).thenReturn("doc-1");
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.getString(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> data.get(invocation.getArgument(0)) instanceof String
                        ? data.get(invocation.getArgument(0))
                        : null);
        when(snapshot.get(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> data.get(invocation.getArgument(0)));
        when(snapshot.getTimestamp(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> (Timestamp) data.get(invocation.getArgument(0)));

        SettableApiFuture<DocumentSnapshot> getFuture = SettableApiFuture.create();
        getFuture.set(snapshot);
        when(document.get()).thenReturn(getFuture);
    }
}

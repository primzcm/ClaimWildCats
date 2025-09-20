package com.claimwildcats.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.claimwildcats.api.domain.ItemDetail;
import com.claimwildcats.api.domain.ItemStatus;
import com.claimwildcats.api.domain.ItemSummary;
import com.claimwildcats.api.dto.CreateFoundItemRequest;
import com.claimwildcats.api.dto.CreateLostItemRequest;
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

    private final FirebaseFacade firebaseFacade = mock(FirebaseFacade.class);
    private final Firestore firestore = mock(Firestore.class);
    private final CollectionReference collection = mock(CollectionReference.class);
    private final DocumentReference document = mock(DocumentReference.class);

    private ItemService itemService;

    @BeforeEach
    void setUp() {
        itemService = new ItemService(firebaseFacade);
        lenient().when(firebaseFacade.getFirestore()).thenReturn(Optional.of(firestore));
        lenient().when(firestore.collection("items")).thenReturn(collection);
        lenient().when(collection.document()).thenReturn(document);
        lenient().when(collection.document(org.mockito.ArgumentMatchers.anyString())).thenReturn(document);
    }

    @Test
    void createLostItem_persistsExpectedFields() throws Exception {
        CreateLostItemRequest request = new CreateLostItemRequest(
                "Blue Backpack",
                "Bags",
                "Library Atrium",
                Instant.parse("2024-03-01T10:15:30Z"),
                "Canvas bag with laptop",
                "Blue",
                "North Face",
                true,
                "Email",
                List.of("https://example.com/img1"));

        prepareFirestoreResult(Map.of(
                "title", "Blue Backpack",
                "category", "Bags",
                "description", "Canvas bag with laptop",
                "location", "Library Atrium",
                "status", "LOST",
                "custody", "WITH_OWNER",
                "photoUrls", List.of("https://example.com/img1"),
                "reportedAt", Timestamp.now(),
                "reporterId", "user-1"));

        ItemDetail detail = itemService.createLostItem(request, "user-1");

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        org.mockito.Mockito.verify(document).set(captor.capture());
        Map<String, Object> stored = captor.getValue();

        assertThat(stored)
                .containsEntry("title", "Blue Backpack")
                .containsEntry("status", "LOST")
                .containsEntry("reporterId", "user-1")
                .containsEntry("rewardOffered", true);

        assertThat(detail.status()).isEqualTo(ItemStatus.LOST);
        assertThat(detail.title()).isEqualTo("Blue Backpack");
    }

    @Test
    void updateStatus_updatesFirestoreDocument() throws Exception {
        prepareFirestoreResult(Map.of(
                "title", "Blue Backpack",
                "category", "Bags",
                "description", "Canvas bag with laptop",
                "location", "Library Atrium",
                "status", "RESOLVED",
                "custody", "WITH_OWNER",
                "photoUrls", List.of(),
                "reportedAt", Timestamp.now(),
                "reporterId", "user-1"));

        ItemDetail detail = itemService.updateStatus("doc-1", new UpdateItemStatusRequest(ItemStatus.RESOLVED, "Delivered"));
        assertThat(detail.status()).isEqualTo(ItemStatus.RESOLVED);
    }

    @Test
    void browseItems_fallbackWhenNoFirestore() {
        when(firebaseFacade.getFirestore()).thenReturn(Optional.empty());
        List<ItemSummary> items = itemService.browseItems();
        assertThat(items).isNotEmpty();
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
        when(snapshot.getString(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> data.get(invocation.getArgument(0)));
        when(snapshot.getDouble(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> {
                    Object value = data.get(invocation.getArgument(0));
                    return value instanceof Double d ? d : null;
                });
        when(snapshot.get("photoUrls")).thenReturn(data.get("photoUrls"));
        when(snapshot.getTimestamp(org.mockito.ArgumentMatchers.anyString()))
                .thenAnswer(invocation -> (Timestamp) data.get(invocation.getArgument(0)));

        SettableApiFuture<DocumentSnapshot> getFuture = SettableApiFuture.create();
        getFuture.set(snapshot);
        when(document.get()).thenReturn(getFuture);
    }
}


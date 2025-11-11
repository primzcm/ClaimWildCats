package com.claimwildcats.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.claimwildcats.api.domain.ClaimStatus;
import com.claimwildcats.api.domain.ClaimSummary;
import com.claimwildcats.api.dto.ClaimItemRequest;
import com.google.api.core.SettableApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ClaimServiceTest {

    private final FirebaseFacade firebaseFacade = mock(FirebaseFacade.class);
    private final Firestore firestore = mock(Firestore.class);
    private final CollectionReference collection = mock(CollectionReference.class);
    private final DocumentReference document = mock(DocumentReference.class);
    private final Query query = mock(Query.class);
    private final QuerySnapshot querySnapshot = mock(QuerySnapshot.class);

    private ClaimService claimService;

    @BeforeEach
    void setUp() {
        claimService = new ClaimService(firebaseFacade);
        lenient().when(firebaseFacade.getFirestore()).thenReturn(Optional.of(firestore));
        lenient().when(firestore.collection("claims")).thenReturn(collection);
        lenient().when(collection.whereEqualTo(anyString(), any())).thenReturn(query);
        lenient().when(query.whereEqualTo(anyString(), any())).thenReturn(query);
        lenient().when(query.orderBy(anyString(), any())).thenReturn(query);
        SettableApiFuture<QuerySnapshot> future = SettableApiFuture.create();
        future.set(querySnapshot);
        lenient().when(query.get()).thenReturn(future);
        lenient().when(querySnapshot.getDocuments()).thenReturn(List.of());
    }

    @Test
    void submitClaim_savesPayloadToFirestore() throws Exception {
        ClaimItemRequest request = new ClaimItemRequest(
                "Blue keychain",
                "Has initials",
                List.of("gs://bucket/claims/item-1/proof.jpg"));
        when(collection.document()).thenReturn(document);

        prepareDocumentMock(Map.of(
                "itemId", "item-1",
                "claimantId", "user-9",
                "status", ClaimStatus.PENDING.name(),
                "submittedAt", Timestamp.now()));

        ClaimSummary summary = claimService.submitClaim("item-1", request, "user-9");

        ArgumentCaptor<Map<String, Object>> captor = ArgumentCaptor.forClass(Map.class);
        verify(document).set(captor.capture());
        Map<String, Object> stored = captor.getValue();
        assertThat(stored)
                .containsEntry("itemId", "item-1")
                .containsEntry("claimantId", "user-9")
                .containsEntry("status", ClaimStatus.PENDING.name());

        assertThat(summary.itemId()).isEqualTo("item-1");
        assertThat(summary.status()).isEqualTo(ClaimStatus.PENDING);
    }

    @Test
    void submitClaim_rejectsInvalidAttachments() {
        ClaimItemRequest request = new ClaimItemRequest(
                "Blue keychain",
                "Details",
                List.of("https://example.com/proof.jpg"));

        assertThrows(IllegalArgumentException.class, () -> claimService.submitClaim("item-1", request, "user-9"));
    }

    @Test
    void listClaimsForUser_fallsBackWhenFirestoreUnavailable() {
        when(firebaseFacade.getFirestore()).thenReturn(Optional.empty());
        List<ClaimSummary> claims = claimService.listClaimsForUser("user-77");
        assertThat(claims).isNotEmpty();
    }

    private void prepareDocumentMock(Map<String, Object> data) throws Exception {
        ArgumentCaptor<Map<String, Object>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        SettableApiFuture<com.google.cloud.firestore.WriteResult> writeFuture = SettableApiFuture.create();
        writeFuture.set(mock(com.google.cloud.firestore.WriteResult.class));
        when(document.set(mapCaptor.capture())).thenReturn(writeFuture);

        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.getId()).thenReturn("claim-1");
        when(snapshot.getString(anyString())).thenAnswer(invocation -> data.get(invocation.getArgument(0)));
        when(snapshot.getTimestamp(anyString())).thenAnswer(invocation -> (Timestamp) data.get(invocation.getArgument(0)));
        when(snapshot.get(anyString())).thenReturn(null);

        SettableApiFuture<DocumentSnapshot> getFuture = SettableApiFuture.create();
        getFuture.set(snapshot);
        when(document.get()).thenReturn(getFuture);
    }
}


package com.claimwildcats.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.claimwildcats.api.domain.UserProfile;
import com.claimwildcats.api.domain.UserRole;
import com.google.api.core.SettableApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private final FirebaseFacade firebaseFacade = mock(FirebaseFacade.class);
    private final Firestore firestore = mock(Firestore.class);
    private final CollectionReference usersCollection = mock(CollectionReference.class);
    private final DocumentReference userDocument = mock(DocumentReference.class);
    private final ItemService itemService = mock(ItemService.class);
    private final ClaimService claimService = mock(ClaimService.class);

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(itemService, claimService, firebaseFacade);
        lenient().when(itemService.listReportsForUser("user-1")).thenReturn(List.of());
        lenient().when(claimService.listClaimsForUser("user-1")).thenReturn(List.of());
        lenient().when(firebaseFacade.getFirestore()).thenReturn(Optional.of(firestore));
        lenient().when(firestore.collection("users")).thenReturn(usersCollection);
        lenient().when(usersCollection.document("user-1")).thenReturn(userDocument);
    }

    @Test
    void getProfile_usesFirestoreDetailsWhenAvailable() throws Exception {
        DocumentSnapshot snapshot = mock(DocumentSnapshot.class);
        when(snapshot.exists()).thenReturn(true);
        when(snapshot.getString("fullName")).thenReturn("Jordan Wildcat");
        when(snapshot.getString("email")).thenReturn("jordan@campus.edu");
        when(snapshot.getBoolean("emailVerified")).thenReturn(true);
        when(snapshot.getString("role")).thenReturn("ADMIN");
        when(snapshot.getTimestamp("createdAt")).thenReturn(Timestamp.now());

        SettableApiFuture<DocumentSnapshot> future = SettableApiFuture.create();
        future.set(snapshot);
        when(userDocument.get()).thenReturn(future);

        UserProfile profile = userService.getProfile("user-1");
        assertThat(profile.fullName()).isEqualTo("Jordan Wildcat");
        assertThat(profile.email()).isEqualTo("jordan@campus.edu");
        assertThat(profile.role()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void getProfile_fallbackWhenFirestoreMissing() {
        when(firebaseFacade.getFirestore()).thenReturn(Optional.empty());
        UserProfile profile = userService.getProfile("user-1");
        assertThat(profile.id()).isEqualTo("user-1");
        assertThat(profile.role()).isEqualTo(UserRole.USER);
    }
}

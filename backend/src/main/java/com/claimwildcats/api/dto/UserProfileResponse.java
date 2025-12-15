package com.claimwildcats.api.dto;

import com.claimwildcats.api.entity.User;
import com.claimwildcats.api.entity.UserProfile;

public class UserProfileResponse {

    private Long userId;
    private String name;
    private String email;
    private String department;
    private String contactNumber;
    private String profileImageUrl;
    private String coverImageUrl;

    public UserProfileResponse(User user, UserProfile profile) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.department = user.getDepartment();
        this.contactNumber = user.getContactNumber();
        this.profileImageUrl = profile.getProfileImageUrl();
        this.coverImageUrl = profile.getCoverImageUrl();
    }

        public Long getUserId() { return userId; }
        public String getName() { return name; }
        public String getEmail() { return email; }
        public String getDepartment() { return department; }
        public String getContactNumber() { return contactNumber; }
        public String getProfileImageUrl() { return profileImageUrl; }
        public String getCoverImageUrl() { return coverImageUrl; }

}

import { PageLayout } from "../components/PageLayout";
import { ProfileHeader } from "../components/ProfileHeader";
import { ProfileContactCard } from "../components/ProfileContactCard";
import { ReportCard } from "../components/ReportCard";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState } from "react";
import { api } from "../api/client";

export function UserProfile() {
  const { user } = useAuth();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const userId = user?.userId || user?.id;  // ✅ SINGLE SOURCE OF TRUTH

  // 🔼 Upload profile image
  const uploadProfile = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    const updated = await api(
      `/api/profile/${userId}/profile-image`,
      {
        method: "POST",
        body: formData
      }
    );

    setProfile(updated);
  };

  // 🔼 Upload cover image
  const uploadCover = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("file", file);

    const updated = await api(
      `/api/profile/${userId}/cover-image`,
      {
        method: "POST",
        body: formData
      }
    );

    setProfile(updated);
  };

  useEffect(() => {
  console.log("USER ID USED:", userId);

  if (!userId) {
    console.log("NO USER ID — STOPPING");
    setLoading(false);
    return;
  }

  setLoading(true);

  api(`/api/profile/${userId}`)
    .then((data) => {
      console.log("PROFILE LOADED:", data);
      setProfile(data);
    })
    .catch((err) => {
      console.error("Failed to load profile", err);
      setProfile(null);
    })
    .finally(() => {
      console.log("LOADING FINISHED");
      setLoading(false);
    });
}, [userId]);


  if (!user) return <p>Please log in.</p>;

  const mockReports = [
    { id: 1, title: "Umbrella", status: "FOUND", image: "/src/icons/front.png" },
    { id: 2, title: "Wallet", status: "LOST", image: "/src/icons/front.png" },
  ];

  return (
    <PageLayout title="My Profile">
      <ProfileHeader
        profile={profile}
        loading={loading}
        onUploadProfile={uploadProfile}
        onUploadCover={uploadCover}
      />

      <ProfileContactCard profile={profile} />

      <section>
        <h3 className="text-xl font-bold text-brown mb-4">
          Filed Reports
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {mockReports.map((r) => (
            <ReportCard key={r.id} report={r} />
          ))}
        </div>
      </section>
    </PageLayout>
  );
}

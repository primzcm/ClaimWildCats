export function ProfileHeader({
  profile,
  loading,
  onUploadProfile,
  onUploadCover
}) {
  if (loading) {
    return (
      <div className="rounded-3xl bg-white shadow-card p-8">
        <p>Loading profile...</p>
      </div>
    );
  }

  if (!profile || !profile.user) {
    return (
      <div className="rounded-3xl bg-white shadow-card p-8">
        <p>Profile not available</p>
      </div>
    );
  }

  const user = profile.user;

  return (
    <div className="rounded-3xl overflow-hidden bg-white shadow-card border border-stone-200">

      {/* COVER */}
      <div className="h-48 bg-burgundy relative">
        <img
          src={profile.coverImageUrl || "/src/icons/back_maroon.png"}
          className="w-full h-full object-cover"
        />

        <label className="absolute top-3 right-3 bg-black/60 text-white px-3 py-1 rounded cursor-pointer">
          Change cover
          <input type="file" hidden onChange={onUploadCover} />
        </label>
      </div>

      {/* PROFILE */}
      <div className="px-8 pb-6 relative">
        <div className="-mt-16 w-32 h-32 rounded-full border-4 border-white bg-stone-200 overflow-hidden shadow-lg relative">
          <img
            src={profile.profileImageUrl || "/src/icons/logo.png"}
            className="w-full h-full object-cover"
          />

          <label className="absolute bottom-1 right-1 bg-burgundy text-white w-8 h-8 rounded-full flex items-center justify-center cursor-pointer">
            +
            <input type="file" hidden onChange={onUploadProfile} />
          </label>
        </div>

        <div className="mt-4">
          <h2 className="text-3xl font-extrabold text-burgundyDeep">
            {user.name}
          </h2>
          <p className="text-brown/70 font-medium">
            {user.department}
          </p>
        </div>
      </div>
    </div>
  );
}
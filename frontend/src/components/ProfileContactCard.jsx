export function ProfileContactCard({ profile }) {
  // Guard: profile or user not loaded yet
  if (!profile || !profile.user) {
    return (
      <div className="bg-burgundy text-cream rounded-2xl p-6 shadow-card">
        <p className="text-sm italic">Contact details unavailable</p>
      </div>
    );
  }

  const user = profile.user;

  return (
    <div className="bg-burgundy text-cream rounded-2xl p-6 shadow-card">
      <h3 className="font-bold text-lg mb-4">Contact Details</h3>

      <div className="space-y-2 text-sm">
        <p>
          <span className="font-semibold">Institutional Email:</span><br />
          {user.email}
        </p>

        <p>
          <span className="font-semibold">Contact Number:</span><br />
          {user.contactNumber || "Not provided"}
        </p>
      </div>
    </div>
  );
}

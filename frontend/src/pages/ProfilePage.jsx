import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { PageLayout } from '../components/PageLayout';
import { useAuth } from '../context/AuthContext';

export function ProfilePage() {
  const { user, login } = useAuth(); // login is used here to update the user session after editing
  const [reports, setReports] = useState([]);
  const [isEditing, setIsEditing] = useState(false);
  const [loadingReports, setLoadingReports] = useState(true);

  // Edit Form State
  const [editName, setEditName] = useState('');
  const [editEmail, setEditEmail] = useState('');

  // 1. Load Data on Mount
  useEffect(() => {
    if (user) {
      // Pre-fill edit form
      setEditName(user.fullName || user.full_name || '');
      setEditEmail(user.email || '');

      // Fetch "My Reports" from Java Backend
      // This endpoint is defined in your UserController: @GetMapping("/{userId}/reports")
      fetch(`http://localhost:8080/api/users/${user.id}/reports`)
        .then((res) => res.json())
        .then((data) => {
          setReports(data);
          setLoadingReports(false);
        })
        .catch((err) => {
          console.error("Error fetching reports:", err);
          setLoadingReports(false);
        });
    }
  }, [user]);

  // 2. Handle Profile Update
  const handleSaveProfile = async (e) => {
    e.preventDefault();
    try {
      const response = await fetch(`http://localhost:8080/api/users/${user.id}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ full_name: editName, email: editEmail })
      });

      if (response.ok) {
        // Update the global auth state immediately so the Header updates too
        const updatedUser = { ...user, fullName: editName, email: editEmail };
        login(updatedUser); 
        setIsEditing(false);
      } else {
        alert("Failed to update profile.");
      }
    } catch (error) {
      console.error(error);
    }
  };

  if (!user) return <div>Please log in first.</div>;

  return (
    <PageLayout
      title="My Profile"
      description="Manage your account details and view the status of your reports."
    >
      <div className="space-y-10">
        
        {/* --- SECTION 1: ACCOUNT DETAILS --- */}
        <section className="bg-white rounded-3xl shadow-card p-8 border border-stone-200">
          <div className="flex flex-col md:flex-row gap-8 items-start">
            
            {/* Avatar Circle */}
            <div className="h-24 w-24 rounded-full bg-burgundy/10 flex items-center justify-center text-4xl shrink-0">
               {user.photoURL ? (
                 <img src={user.photoURL} alt="Profile" className="h-full w-full rounded-full object-cover"/>
               ) : (
                 <span>🐱</span>
               )}
            </div>

            <div className="flex-1 w-full">
              {isEditing ? (
                /* EDIT FORM */
                <form onSubmit={handleSaveProfile} className="space-y-4 max-w-md">
                  <div>
                    <label className="text-xs font-bold text-stone-500 uppercase">Full Name</label>
                    <input 
                      type="text" 
                      value={editName}
                      onChange={(e) => setEditName(e.target.value)}
                      className="block w-full border border-stone-300 rounded-lg px-4 py-2 mt-1 focus:ring-2 focus:ring-gold focus:outline-none"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-bold text-stone-500 uppercase">Email (Read Only)</label>
                    <input 
                      type="email" 
                      value={editEmail}
                      disabled
                      className="block w-full border border-stone-200 bg-stone-100 text-stone-500 rounded-lg px-4 py-2 mt-1 cursor-not-allowed"
                    />
                  </div>
                  <div className="flex gap-3 pt-2">
                    <button type="submit" className="bg-burgundy text-white px-6 py-2 rounded-full text-sm font-bold hover:bg-burgundyDeep transition">Save Changes</button>
                    <button type="button" onClick={() => setIsEditing(false)} className="bg-white border border-stone-300 text-stone-600 px-6 py-2 rounded-full text-sm font-bold hover:bg-stone-50 transition">Cancel</button>
                  </div>
                </form>
              ) : (
                /* DISPLAY MODE */
                <div className="flex justify-between items-start">
                  <div>
                    <h2 className="text-3xl font-extrabold text-burgundyDeep">
                      {user.fullName || user.full_name || user.email}
                    </h2>
                    <p className="text-brown/70 font-medium">{user.email}</p>
                    <div className="mt-3 inline-flex bg-gold/20 text-brown px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider">
                      {user.role || 'Member'}
                    </div>
                  </div>
                  <button 
                    onClick={() => setIsEditing(true)}
                    className="text-burgundy font-bold text-sm hover:underline hover:text-gold transition"
                  >
                    Edit Profile
                  </button>
                </div>
              )}
            </div>
          </div>
        </section>

        {/* --- SECTION 2: FILED REPORTS --- */}
        <section>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-brown">Filed Reports</h2>
            <Link to="/items/new/lost" className="text-sm font-bold text-burgundy hover:text-gold transition">
              + File New Report
            </Link>
          </div>

          {loadingReports ? (
            <div className="p-10 text-center text-stone-400">Loading your reports...</div>
          ) : reports.length === 0 ? (
            <div className="bg-white rounded-xl border-2 border-dashed border-stone-200 p-12 text-center">
              <p className="text-stone-500 font-medium">You haven't filed any reports yet.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {reports.map((item) => (
                <Link 
                  key={item.id} 
                  to={`/items/${item.id}`} 
                  className="group block bg-white rounded-2xl shadow-soft hover:shadow-card transition overflow-hidden border border-stone-100"
                >
                  {/* Image Area */}
                  <div className="h-40 bg-stone-100 relative overflow-hidden">
                    {item.docUrls && item.docUrls.length > 0 ? (
                      <img src={item.docUrls[0]} alt={item.title} className="w-full h-full object-cover group-hover:scale-105 transition duration-500" />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-stone-300 text-4xl bg-stone-50">
                        📦
                      </div>
                    )}
                    {/* Status Badge */}
                    <div className={`absolute top-3 right-3 px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wide shadow-sm ${
                      item.status === 'CLAIMED' ? 'bg-green-500 text-white' : 'bg-gold text-white'
                    }`}>
                      {item.status}
                    </div>
                  </div>
                  
                  {/* Content Area */}
                  <div className="p-5">
                    <h3 className="font-bold text-lg text-burgundyDeep group-hover:text-gold transition mb-1 truncate">
                      {item.title}
                    </h3>
                    <p className="text-sm text-stone-500 mb-4 flex items-center gap-1">
                      📍 {item.locationText || "Unknown Location"}
                    </p>

                    {/* Claims Indicator (Based on your Requirement) */}
                    {item.status !== 'OPEN' ? (
                       <div className="bg-burgundy/5 border border-burgundy/10 p-3 rounded-lg">
                          <p className="text-xs font-bold text-burgundy">
                            ⚠️ This item has activity.
                          </p>
                          <p className="text-[10px] text-stone-500 mt-1">
                            Click to view claimer details and descriptions.
                          </p>
                       </div>
                    ) : (
                      <div className="text-xs text-stone-400 italic py-2">
                        No claims filed yet.
                      </div>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          )}
        </section>

      </div>
    </PageLayout>
  );
}
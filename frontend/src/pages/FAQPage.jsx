import "./FAQPage.css";
import { useState } from "react";
import { FaChevronDown, FaChevronLeft } from "react-icons/fa";
import { useNavigate } from "react-router-dom";

export default function FAQPage() {
  const navigate = useNavigate();
  const [openIndex, setOpenIndex] = useState(null);

  const toggleFAQ = (i) => {
    setOpenIndex(openIndex === i ? null : i);
  };

  const faqs = [
    {
      title: "About the Lost & Found System",
      questions: [
        {
          q: "What is this website for?",
          a: "This website helps people report lost items, post found items, and connect owners with finders safely and easily."
        },
        {
          q: "Is this service free?",
          a: "Yes. Posting lost or found items is free for all users."
        }
      ]
    },
    {
      title: "Creating an Account",
      questions: [
        {
          q: "Do I need an account to report an item?",
          a: "Yes. An account is required so you can update your post, receive messages, and verify ownership."
        },
        {
          q: "What if I forgot my password?",
          a: "You can click “Forgot Password” on the login page and follow the instructions."
        }
      ]
    },
    {
      title: "Posting a Lost Item",
      questions: [
        {
          q: "How do I report something I lost?",
          a: "Go to “Report Lost Item”, fill out the details (description, date, location, photo), and submit the form."
        },
        {
          q: "What information should I include?",
          a: "Include a clear description, last known location, date/time lost, photos if available, and any unique identifiers."
        }
      ]
    },
    {
      title: "Posting a Found Item",
      questions: [
        {
          q: "How do I post that I found something?",
          a: "Go to “Report Found Item”, provide descriptions and photo(s), and submit the post."
        },
        {
          q: "What if I want to verify the real owner first?",
          a: "Ask for details only the owner would know, proof of ownership, or photos. Never hand over items without verification."
        }
      ]
    },
    {
      title: "Searching for Items",
      questions: [
        {
          q: "How do I find my lost item?",
          a: "Use the Search page to filter by category, location, date, and item type. You can also browse the Lost or Found sections."
        }
      ]
    },
    {
      title: "Contacting Finders or Owners",
      questions: [
        {
          q: "How do I contact someone who posted an item?",
          a: "Open the item post and click the Contact Poster button. Messages are private — your email and phone number stay hidden."
        },
        {
          q: "Is it safe to meet in person?",
          a: "Meet in public places, preferably with a friend, during daytime. Never share sensitive personal information."
        }
      ]
    },
    {
      title: "Updating or Deleting a Post",
      questions: [
        {
          q: "How do I edit my post?",
          a: "Go to My Posts, select the item, and click Edit."
        },
        {
          q: "How do I remove my post?",
          a: "Go to My Posts and click Delete. Posts should be removed once the item is reunited."
        }
      ]
    },
    {
      title: "Safety & Privacy",
      questions: [
        {
          q: "Is my personal information public?",
          a: "No. Only your username and item details are shown. Your contact information stays private."
        },
        {
          q: "What should I avoid posting?",
          a: "Do NOT post your home address, sensitive personal data, or full serial numbers."
        }
      ]
    },
    {
      title: "Reporting Issues",
      questions: [
        {
          q: "What if I see a suspicious or false posting?",
          a: "Click “Report Post” on the item and describe the issue."
        },
        {
          q: "Who do I contact for help?",
          a: "Email support@lostfound.com or use the Contact Us page."
        }
      ]
    },
    {
      title: "Item Claims",
      questions: [
        {
          q: "How do I prove an item is mine?",
          a: "Provide color, size, unique markings, photos, receipts, or private serial numbers. The finder may verify before returning the item."
        }
      ]
    }
  ];

  return (
    <div className="faq-container">
      <button className="faq-back-btn flex items-center gap-3" onClick={() => navigate(-1)}>
                <img 
                    src="src/icons/back_maroon.png" 
                    alt="back icon"
                    width={40}
                    height={40}
                    className="mr-3"
                />
            <span className="faq-back-text">Back</span>
        </button>


      <h1 className="faq-title">Help & FAQ</h1>

      <div className="faq-list">
        {faqs.map((section, index) => (
          <div key={index} className="faq-section">
            <button className="faq-section-header" onClick={() => toggleFAQ(index)}>
            <span style={{ color: "#FFE9B3" }}>
                {index + 1}. {section.title}
            </span>
            <FaChevronDown className={openIndex === index ? "rotate" : ""} />
        </button>


            <div className={`faq-content ${openIndex === index ? "open" : ""}`}>
              {section.questions.map((item, idx) => (
                <div key={idx} className="faq-item">
                  <h3>{item.q}</h3>
                  <p>{item.a}</p>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

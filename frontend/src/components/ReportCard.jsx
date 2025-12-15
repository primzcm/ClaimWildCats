import { Link } from "react-router-dom";

export function ReportCard({ report }) {
  const statusColor =
    report.status === "FOUND"
      ? "bg-green-500"
      : "bg-red-500";

  return (
    <Link
      to="#"
      className="bg-white rounded-xl shadow-card overflow-hidden border border-stone-200 hover:shadow-lg transition"
    >
      <div className="h-40 relative">
        <img
          src={report.image}
          alt={report.title}
          className="w-full h-full object-cover"
        />
        <span
          className={`absolute top-3 left-3 ${statusColor} text-white text-xs px-3 py-1 rounded-full font-bold`}
        >
          {report.status}
        </span>
      </div>

      <div className="p-4">
        <h4 className="font-bold text-burgundyDeep truncate">
          {report.title}
        </h4>
        <p className="text-xs text-stone-500 mt-1">
          View Details
        </p>
      </div>
    </Link>
  );
}

// 1. NO MORE FIREBASE IMPORTS
// We communicate directly with Java Backend now

export function generateAttachmentId() {
  return crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

export function formatFileSize(bytes) {
  if (!Number.isFinite(bytes)) return '';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 102.4) / 10} KB`;
  return `${Math.round(bytes / 1024 / 102.4) / 10} MB`;
}

/**
 * Uploads files to the Java Backend (Port 8080)
 * Replaces the old Firebase storage logic.
 */
export async function uploadFiles(basePath, attachments) {
  // basePath is ignored now because Java decides where to save files, 
  // but we keep the argument so we don't break your existing code calling this function.
  
  if (!attachments || attachments.length === 0) {
    return [];
  }

  const uploadedResults = [];

  try {
    for (let index = 0; index < attachments.length; index += 1) {
      const { file } = attachments[index];
      
      // 1. Prepare the file for sending
      const formData = new FormData();
      formData.append('file', file);

      // 2. Send to Java Backend
      const response = await fetch('http://localhost:8080/api/uploads', {
        method: 'POST',
        body: formData, // Browser automatically sets Content-Type to multipart/form-data
      });

      if (!response.ok) {
        throw new Error(`Failed to upload ${file.name}`);
      }

      // 3. Get the URL back from Java
      const data = await response.json(); // Expects { "url": "http://localhost:8080/..." }

      // 4. Format the result to match what your app expects
      // We map 'storageUri' to the HTTP URL now, so images display directly.
      uploadedResults.push({ 
        ref: null, // No Firebase ref anymore
        storageUri: data.url // This URL is saved to your MySQL database
      });
    }

    return uploadedResults;

  } catch (error) {
    console.error("Upload error:", error);
    throw error;
  }
}

export async function cleanupUploads(entries) {
  // Since we are moving away from Firebase, implementing "undo upload" 
  // requires a specific DELETE endpoint in Java. 
  // For now, we leave this empty to prevent errors.
  console.log("Cleanup skipped (Firebase removed)");
  return;
}
import { ref, uploadBytes, deleteObject } from 'firebase/storage';
import { storage } from '../lib/firebase';

export function generateAttachmentId() {
  return crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random().toString(36).slice(2)}`;
}

export function formatFileSize(bytes) {
  if (!Number.isFinite(bytes)) return '';
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 102.4) / 10} KB`;
  return `${Math.round(bytes / 1024 / 102.4) / 10} MB`;
}

function buildObjectName(file, index) {
  const extensionMatch = file.name.match(/\.[^./]+$/);
  const extension = extensionMatch ? extensionMatch[0].toLowerCase() : '';
  const base = file.name.replace(/\.[^./]+$/, '')
    .toLowerCase()
    .replace(/[^a-z0-9-]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '') || 'attachment';
  return `${base}-${Date.now()}-${index}${extension}`;
}

export async function uploadFiles(basePath, attachments) {
  if (!basePath) {
    throw new Error('A Storage base path is required.');
  }
  if (!attachments || attachments.length === 0) {
    return [];
  }

  const uploads = [];
  try {
    for (let index = 0; index < attachments.length; index += 1) {
      const { file } = attachments[index];
      const objectName = buildObjectName(file, index);
      const objectPath = `${basePath}/${objectName}`;
      const storageRef = ref(storage, objectPath);
      await uploadBytes(storageRef, file, { contentType: file.type });
      uploads.push({ ref: storageRef, storageUri: `gs://${storageRef.bucket}/${objectPath}` });
    }
    return uploads;
  } catch (error) {
    await cleanupUploads(uploads);
    throw error;
  }
}

export async function cleanupUploads(entries) {
  if (!entries || entries.length === 0) {
    return;
  }
  await Promise.all(entries.map(({ ref: storageRef }) => deleteObject(storageRef).catch(() => {})));
}


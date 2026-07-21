import React, { useState } from 'react';
import './css/FileItem.css';
import { FiFolder, FiFile, FiEdit2, FiTrash2, FiArrowRight } from 'react-icons/fi';

const FileItem = ({ file, onOpenFolder, onRename, onDelete }) => {
  const [isRenaming, setIsRenaming] = useState(false);
  const [newName, setNewName] = useState(file.name);

  const handleRenameSubmit = () => {
    if (newName.trim() && newName !== file.name) {
      onRename(file.id, newName);
    }
    setIsRenaming(false);
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="file-item">
      <div className="file-name-col">
        {/* Icon instead of emoji */}
        <span className="file-icon">
          {file.isFolder ? <FiFolder size={20} /> : <FiFile size={20} />}
        </span>

        {isRenaming ? (
          <input
            type="text"
            className="rename-input"
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleRenameSubmit()}
            autoFocus
          />
        ) : (
          <span className="file-name-text">{file.name}</span>
        )}
      </div>

      <div className="file-type-col">
        {file.isFolder ? 'Folder' : 'File'}
      </div>

      <div className="file-date-col">
        {formatDate(file.updatedAt)}
      </div>

      <div className="file-actions-col">
        {file.isFolder && (
          <button
            className="action-btn open-btn"
            onClick={() => onOpenFolder(file)}
            title="Open folder"
          >
            <FiArrowRight size={16} />
          </button>
        )}
        <button
          className="action-btn rename-btn"
          onClick={() => setIsRenaming(true)}
          title="Rename"
        >
          <FiEdit2 size={16} />
        </button>
        <button
          className="action-btn delete-btn"
          onClick={() => {
            if (window.confirm('Are you sure you want to delete this?')) {
              onDelete(file.id);
            }
          }}
          title="Delete"
        >
          <FiTrash2 size={16} />
        </button>
      </div>
    </div>
  );
};

export default FileItem;
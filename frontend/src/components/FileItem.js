import React, { useState } from 'react';
import './css/FileItem.css';
import { FiFolder, FiFile, FiEdit2, FiTrash2, FiArrowRight } from 'react-icons/fi';

const FileItem = ({ file, onOpenFolder, onRename, onDelete, actionLoading }) => {
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
  const isBusy = Boolean(actionLoading);
  const isRenamingFile = actionLoading === `rename:${file.id}`;
  const isDeletingFile = actionLoading === `delete:${file.id}`;

  return (
    <div className="file-item">
      <div className="file-name-col">
        {/* Icon instead of emoji */}
        <span className="file-icon">
          {file.folder ? <FiFolder size={20} /> : <FiFile size={20} />}
        </span>

        {isRenaming ? (
          <input
            type="text"
            className="rename-input"
            value={newName}
            onBlur={handleRenameSubmit}
            onChange={(e) => setNewName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleRenameSubmit()}
            disabled={isBusy}
            autoFocus
          />
        ) : (
          file.folder ? (
            <button
              type="button"
              className="file-name-text folder-name-button"
              onClick={() => onOpenFolder(file)}
              title={`Open ${file.name}`}
              disabled={isBusy}
            >
              {file.name}
            </button>
          ) : (
            <span className="file-name-text">{file.name}</span>
          )
        )}
      </div>

      <div className="file-type-col">
        {file.folder ? 'Folder' : 'File'}
      </div>

      <div className="file-date-col">
        {formatDate(file.updatedAt)}
      </div>

      <div className="file-actions-col">
        {file.folder && (
          <button
            className="action-btn open-btn"
            onClick={() => onOpenFolder(file)}
            title="Open folder"
            aria-label={`Open ${file.name}`}
          >
            <FiArrowRight size={16} />
          </button>
        )}
        <button
          className="action-btn rename-btn"
          onClick={() => setIsRenaming(true)}
          title="Rename"
          aria-label={`Rename ${file.name}`}
          disabled={isBusy}
        >
          {isRenamingFile ? '...' : <FiEdit2 size={16} />}
        </button>
        <button
          className="action-btn delete-btn"
          onClick={() => {
            if (window.confirm('Are you sure you want to delete this?')) {
              onDelete(file.id);
            }
          }}
          title="Delete"
          aria-label={`Delete ${file.name}`}
          disabled={isBusy}
        >
          {isDeletingFile ? '...' : <FiTrash2 size={16} />}
        </button>
      </div>
    </div>
  );
};

export default FileItem;
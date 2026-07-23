import React from 'react';
import './css/ActionBar.css';
import { FiArrowLeft, FiSearch } from 'react-icons/fi';

const ActionBar = ({
  currentFolder,
  folderPath,
  onNavigateToFolder,
  onGoBack,
  newFileName,
  setNewFileName,
  onCreateFile,
  newFolderName,
  setNewFolderName,
  onCreateFolder,
  searchQuery,
  setSearchQuery,
  suggestions,
  onSelectSuggestion,
  actionLoading
}) => {
  const isBusy = Boolean(actionLoading);
  const isCreatingFile = actionLoading === 'create-file';
  const isCreatingFolder = actionLoading === 'create-folder';

  return (
    <div className="action-bar">
      <div className="breadcrumb">
        {currentFolder && (
          <button className="back-btn" onClick={onGoBack} title="Go back" aria-label="Go back">
            <FiArrowLeft size={20} />
            Back
          </button>
        )}
        <nav className="breadcrumb-path" aria-label="Folder path">
          <button type="button" className="breadcrumb-link" onClick={() => onNavigateToFolder(-1)}>
            Root Folder
          </button>
          {folderPath.map((folder, index) => (
            <React.Fragment key={folder.id}>
              <span className="breadcrumb-separator" aria-hidden="true">/</span>
              <button
                type="button"
                className="breadcrumb-link"
                onClick={() => onNavigateToFolder(index)}
              >
                {folder.name}
              </button>
            </React.Fragment>
          ))}
        </nav>
      </div>

      <div className="action-inputs">
        <div className="input-group">
          <input
            type="text"
            placeholder="File name..."
            value={newFileName}
            disabled={isBusy}
            onChange={(e) => setNewFileName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && onCreateFile()}
          />
          <button onClick={onCreateFile} disabled={isBusy}>
            {isCreatingFile ? 'Creating...' : 'Create File'}
          </button>
        </div>

        <div className="input-group">
          <input
            type="text"
            placeholder="Folder name..."
            value={newFolderName}
            disabled={isBusy}
            onChange={(e) => setNewFolderName(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && onCreateFolder()}
          />
          <button onClick={onCreateFolder} disabled={isBusy}>
            {isCreatingFolder ? 'Creating...' : 'Create Folder'}
          </button>
        </div>
      </div>

      <div className="search-box">
        <FiSearch size={18} />
        <input
          type="text"
          placeholder="Search files..."
          aria-label="Search files"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        {suggestions.length > 0 && (
          <ul className="suggestions">
            {suggestions.map((file) => (
              <li key={file.id}>
                <button type="button" onClick={() => onSelectSuggestion(file.name)}>
                  {file.name}
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
};

export default ActionBar;

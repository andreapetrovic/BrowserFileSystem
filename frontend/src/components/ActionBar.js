import React from 'react';
import './css/ActionBar.css';
import { FiArrowLeft, FiSearch } from 'react-icons/fi';

const ActionBar = ({
  currentFolder,
  onGoBack,
  newFileName,
  setNewFileName,
  onCreateFile,
  newFolderName,
  setNewFolderName,
  onCreateFolder,
  searchQuery,
  setSearchQuery,
  onSearch
}) => {
  return (
    <div className="action-bar">
      <div className="breadcrumb">
        {currentFolder && (
          <button className="back-btn" onClick={onGoBack} title="Go back">
            <FiArrowLeft size={20} />
            Back
          </button>
        )}
        <span className="current-location">
          {currentFolder ? 'Subfolder' : 'Root Folder'}
        </span>
      </div>

      <div className="search-box">
        <FiSearch size={18} />
        <input
          type="text"
          placeholder="Search files..."
          value={searchQuery}
          onChange={(e) => {
            setSearchQuery(e.target.value);
            if (e.target.value.length > 0) {
              onSearch(e.target.value);
            }
          }}
        />
      </div>

      <div className="action-inputs">
        <div className="input-group">
          <input
            type="text"
            placeholder="File name..."
            value={newFileName}
            onChange={(e) => setNewFileName(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && onCreateFile()}
          />
          <button onClick={onCreateFile}>Create File</button>
        </div>

        <div className="input-group">
          <input
            type="text"
            placeholder="Folder name..."
            value={newFolderName}
            onChange={(e) => setNewFolderName(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && onCreateFolder()}
          />
          <button onClick={onCreateFolder}>Create Folder</button>
        </div>
      </div>
    </div>
  );
};

export default ActionBar;
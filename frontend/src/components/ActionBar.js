import React from 'react';
import './css/ActionBar.css';
import { FiArrowLeft } from 'react-icons/fi';  // Import icon
// Other popular icons:
// import { AiOutlineArrowLeft } from 'react-icons/ai';
// import { MdArrowBack } from 'react-icons/md';
// import { BiArrowBack } from 'react-icons/bi';

const ActionBar = ({
  currentFolder,
  onGoBack,
  newFileName,
  setNewFileName,
  onCreateFile,
  newFolderName,
  setNewFolderName,
  onCreateFolder
}) => {
  return (
    <div className="action-bar">
      <div className="breadcrumb">
        {currentFolder && (
          <button className="back-btn" onClick={onGoBack} title="Go back">
            <FiArrowLeft size={20} />  {/* Icon with size */}
            Back
          </button>
        )}
        <span className="current-location">
          {currentFolder ? 'Subfolder' : 'Root Folder'}
        </span>
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
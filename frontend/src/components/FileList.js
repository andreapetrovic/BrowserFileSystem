import React from 'react';
import './css/FileList.css';
import FileItem from './FileItem';

const FileList = ({ files, onOpenFolder, onRename, onDelete, actionLoading }) => {
  if (files.length === 0) {
    return <div className="empty-state">No files or folders here</div>;
  }

  return (
    <div className="file-list">
      <div className="file-list-header">
        <div className="file-name">Name</div>
        <div className="file-type">Type</div>
        <div className="file-date">Modified</div>
        <div className="file-actions">Actions</div>
      </div>
      {files.map((file) => (
        <FileItem
          key={file.id}
          file={file}
          onOpenFolder={onOpenFolder}
          onRename={onRename}
          onDelete={onDelete}
          actionLoading={actionLoading}
        />
      ))}
    </div>
  );
};

export default FileList;

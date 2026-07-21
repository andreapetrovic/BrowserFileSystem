import React, { useState } from 'react';
import './css/FileExplorer.css';
import FileList from './FileList';
import ActionBar from './ActionBar';

const FileExplorer = ({
  files,
  loading,
  currentFolder,
  onCreateFile,
  onCreateFolder,
  onRename,
  onDelete,
  onOpenFolder,
  onGoBack
}) => {
  const [newFileName, setNewFileName] = useState('');
  const [newFolderName, setNewFolderName] = useState('');

  const handleCreateFile = () => {
    if (newFileName.trim()) {
      onCreateFile(newFileName);
      setNewFileName('');
    }
  };

  const handleCreateFolder = () => {
    if (newFolderName.trim()) {
      onCreateFolder(newFolderName);
      setNewFolderName('');
    }
  };

  return (
    <div className="file-explorer">
      <ActionBar
        currentFolder={currentFolder}
        onGoBack={onGoBack}
        newFileName={newFileName}
        setNewFileName={setNewFileName}
        onCreateFile={handleCreateFile}
        newFolderName={newFolderName}
        setNewFolderName={setNewFolderName}
        onCreateFolder={handleCreateFolder}
      />

      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <FileList
          files={files}
          onOpenFolder={onOpenFolder}
          onRename={onRename}
          onDelete={onDelete}
        />
      )}
    </div>
  );
};

export default FileExplorer;

import React, { useEffect, useState } from 'react';
import './css/FileExplorer.css';
import FileList from './FileList';
import ActionBar from './ActionBar';
import api from '../services/api';

const FileExplorer = ({
  files,
  loading,
  currentFolder,
  folderPath,
  onNavigateToFolder,
  searchQuery,
  setSearchQuery,
  onCreateFile,
  onCreateFolder,
  onRename,
  onDelete,
  onOpenFolder,
  onGoBack
}) => {
  const [newFileName, setNewFileName] = useState('');
  const [newFolderName, setNewFolderName] = useState('');
  const [suggestions, setSuggestions] = useState([]);

  useEffect(() => {
    const query = searchQuery.trim();
    if (!query) {
      setSuggestions([]);
      return undefined;
    }

    const timer = setTimeout(async () => {
      try {
        const response = await api.get(
          currentFolder ? '/files/autocomplete/folder' : '/files/autocomplete',
          { params: currentFolder ? { query, parentId: currentFolder } : { query } }
        );
        setSuggestions(response.data);
      } catch {
        setSuggestions([]);
      }
    }, 250);

    return () => clearTimeout(timer);
  }, [searchQuery, currentFolder]);

  const handleSelectSuggestion = (name) => {
    setSearchQuery(name);
    setSuggestions([]);
  };

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
        folderPath={folderPath}
        onNavigateToFolder={onNavigateToFolder}
        onGoBack={onGoBack}
        newFileName={newFileName}
        setNewFileName={setNewFileName}
        onCreateFile={handleCreateFile}
        newFolderName={newFolderName}
        setNewFolderName={setNewFolderName}
        onCreateFolder={handleCreateFolder}
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        suggestions={suggestions}
        onSelectSuggestion={handleSelectSuggestion}
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

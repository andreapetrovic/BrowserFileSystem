import React, { useEffect, useState } from 'react';
import './css/FileExplorer.css';
import FileList from './FileList';
import ActionBar from './ActionBar';
import api from '../services/api';

const FileExplorer = ({
  files,
  loading,
  loadingMore,
  hasNextPage,
  actionLoading,
  onLoadMore,
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
        const response = await api.get('/files/search', {
          params: currentFolder
            ? { name: query, exact: false, parentId: currentFolder }
            : { name: query, exact: false }
        });
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
        actionLoading={actionLoading}
      />

      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <FileList
          files={files}
          onOpenFolder={onOpenFolder}
          onRename={onRename}
          onDelete={onDelete}
          actionLoading={actionLoading}
        />
      )}
      {!loading && hasNextPage && (
        <button type="button" className="load-more-btn" onClick={onLoadMore} disabled={loadingMore}>
          {loadingMore ? 'Loading...' : 'Load more'}
        </button>
      )}
    </div>
  );
};

export default FileExplorer;

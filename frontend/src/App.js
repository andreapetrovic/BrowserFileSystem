import React, { useState, useEffect, useCallback } from 'react';
import './App.css';
import FileExplorer from './components/FileExplorer';
import api from './services/api';

function App() {
  const [files, setFiles] = useState([]);
  const [folderPath, setFolderPath] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [backendError, setBackendError] = useState(false);
  const currentFolder = folderPath.at(-1)?.id ?? null;

  const fetchFiles = useCallback(async () => {
    setLoading(true);
    setError(null);
    setBackendError(false);
    try {
      const query = searchQuery.trim();
      const response = query
        ? await api.get('/files/search', {
            params: currentFolder ? { name: query, exact: true, parentId: currentFolder } : { name: query, exact: true }
          })
        : await api.get('/files', {
            params: currentFolder ? { parentId: currentFolder } : {}
          });
      setFiles(query ? response.data : response.data.content);
      setBackendError(false);
    } catch (err) {
      if (err.response?.status === 404 || err.code === 'ERR_NETWORK' || !err.response) {
        setBackendError(true);
        setError('Cannot connect to backend. Make sure the server is running.');
      } else {
        setError('Failed to load files: ' + err.message);
      }
      setFiles([]);
      console.error(err);
    } finally {
      setLoading(false);
    }
  }, [currentFolder, searchQuery]);

  useEffect(() => {
    fetchFiles();
  }, [fetchFiles]);

  const handleCreateFile = async (name) => {
    try {
      await api.post('/files', { name, ...(currentFolder ? { parentId: currentFolder } : {}) });
      fetchFiles();
    } catch (err) {
      setError('Failed to create file: ' + err.message);
    }
  };

  const handleCreateFolder = async (name) => {
    try {
      await api.post('/folders', { name, ...(currentFolder ? { parentId: currentFolder } : {}) });
      fetchFiles();
    } catch (err) {
      setError('Failed to create folder: ' + err.message);
    }
  };

  const handleRename = async (fileId, newName) => {
    try {
      await api.patch(`/files/${fileId}`, { name: newName });
      fetchFiles();
    } catch (err) {
      setError('Failed to rename: ' + err.message);
    }
  };

  const handleDelete = async (fileId) => {
    try {
      await api.delete(`/files/${fileId}`);
      fetchFiles();
    } catch (err) {
      setError('Failed to delete: ' + err.message);
    }
  };

  const handleOpenFolder = (folder) => {
    setFolderPath((path) => [...path, { id: folder.id, name: folder.name }]);
    setSearchQuery('');
  };

  const handleGoBack = () => {
    setFolderPath((path) => path.slice(0, -1));
    setSearchQuery('');
  };

  const handleNavigateToFolder = (index) => {
    setFolderPath((path) => path.slice(0, index + 1));
    setSearchQuery('');
  };

  return (
    <div className="App">
      <header className="app-header">
        <h1>Browser File System</h1>
      </header>
      <main className="app-main">
        {error && <div className="error-message">{error}</div>}
        {!backendError && (
          <FileExplorer
            files={files}
            loading={loading}
            currentFolder={currentFolder}
            folderPath={folderPath}
            onNavigateToFolder={handleNavigateToFolder}
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
            onCreateFile={handleCreateFile}
            onCreateFolder={handleCreateFolder}
            onRename={handleRename}
            onDelete={handleDelete}
            onOpenFolder={handleOpenFolder}
            onGoBack={handleGoBack}
          />
        )}
      </main>
    </div>
  );
}

export default App;

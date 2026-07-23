import React, { useState, useEffect, useCallback } from 'react';
import './App.css';
import FileExplorer from './components/FileExplorer';
import api from './services/api';

function App() {
  const [files, setFiles] = useState([]);
  const [folderPath, setFolderPath] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasNextPage, setHasNextPage] = useState(false);
  const [actionLoading, setActionLoading] = useState(null);
  const [error, setError] = useState(null);
  const [backendError, setBackendError] = useState(false);
  const currentFolder = folderPath.at(-1)?.id ?? null;

  const fetchFiles = useCallback(async (page = 0, append = false) => {
    if (append) {
      setLoadingMore(true);
    } else {
      setLoading(true);
    }
    setError(null);
    setBackendError(false);
    try {
      const query = searchQuery.trim();
      const response = query
        ? await api.get('/files/search', {
            params: currentFolder
              ? { name: query, exact: true, parentId: currentFolder, page, size: 100 }
              : { name: query, exact: true, page, size: 100 }
          })
        : await api.get('/files', {
            params: currentFolder ? { parentId: currentFolder, page, size: 100 } : { page, size: 100 }
          });
      setFiles((existingFiles) => append ? [...existingFiles, ...response.data.content] : response.data.content);
      setCurrentPage(response.data.number);
      setHasNextPage(!response.data.last);
      setBackendError(false);
    } catch (err) {
      if (err.response?.status === 404 || err.code === 'ERR_NETWORK' || !err.response) {
        setBackendError(true);
        setError('Cannot connect to backend. Make sure the server is running.');
      } else {
        setError('Failed to load files: ' + err.message);
      }
      if (!append) {
        setFiles([]);
        setHasNextPage(false);
      }
      if (process.env.NODE_ENV === 'development') {
        console.error(err);
      }
    } finally {
      if (append) {
        setLoadingMore(false);
      } else {
        setLoading(false);
      }
    }
  }, [currentFolder, searchQuery]);

  useEffect(() => {
    fetchFiles(0);
  }, [fetchFiles]);

  const handleCreateFile = async (name) => {
    if (actionLoading) return;
    setActionLoading('create-file');
    try {
      await api.post('/files', { name, ...(currentFolder ? { parentId: currentFolder } : {}) });
      fetchFiles(0);
    } catch (err) {
      setError('Failed to create file: ' + err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleCreateFolder = async (name) => {
    if (actionLoading) return;
    setActionLoading('create-folder');
    try {
      await api.post('/folders', { name, ...(currentFolder ? { parentId: currentFolder } : {}) });
      fetchFiles(0);
    } catch (err) {
      setError('Failed to create folder: ' + err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleRename = async (fileId, newName) => {
    if (actionLoading) return;
    setActionLoading(`rename:${fileId}`);
    try {
      await api.patch(`/files/${fileId}`, { name: newName });
      fetchFiles(0);
    } catch (err) {
      setError('Failed to rename: ' + err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleDelete = async (fileId) => {
    if (actionLoading) return;
    setActionLoading(`delete:${fileId}`);
    try {
      await api.delete(`/files/${fileId}`);
      fetchFiles(0);
    } catch (err) {
      setError('Failed to delete: ' + err.message);
    } finally {
      setActionLoading(null);
    }
  };

  const handleLoadMore = () => {
    if (hasNextPage && !loadingMore) {
      fetchFiles(currentPage + 1, true);
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
            loadingMore={loadingMore}
            hasNextPage={hasNextPage}
            actionLoading={actionLoading}
            onLoadMore={handleLoadMore}
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

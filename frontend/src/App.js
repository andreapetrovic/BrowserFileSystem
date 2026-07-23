import React, { useState, useEffect, useCallback } from 'react';
import './App.css';
import FileExplorer from './components/FileExplorer';
import api from './services/api';

const getActionErrorMessage = (error, action) => {
  const serverMessage = error.response?.data?.message;
  if (serverMessage) {
    return `${action}: ${serverMessage}`;
  }
  if (error.code === 'ERR_NETWORK' || !error.response) {
    return `${action}: cannot connect to the backend. Make sure the server is running.`;
  }
  return `${action}: please try again.`;
};

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
  const [success, setSuccess] = useState(null);
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
      if (err.code === 'ERR_NETWORK' || !err.response) {
        setBackendError(true);
        setError(getActionErrorMessage(err, 'Could not load files'));
      } else {
        setError(getActionErrorMessage(err, 'Could not load files'));
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

  useEffect(() => {
    if (!success) {
      return undefined;
    }
    const timer = setTimeout(() => setSuccess(null), 5000);
    return () => clearTimeout(timer);
  }, [success]);

  const handleCreateFile = async (name) => {
    if (actionLoading) return;
    setActionLoading('create-file');
    setError(null);
    setSuccess(null);
    try {
      await api.post('/files', { name, ...(currentFolder ? { parentId: currentFolder } : {}) });
      setSuccess(`File “${name}” was created.`);
      fetchFiles(0);
    } catch (err) {
      setError(getActionErrorMessage(err, 'Could not create file'));
    } finally {
      setActionLoading(null);
    }
  };

  const handleCreateFolder = async (name) => {
    if (actionLoading) return;
    setActionLoading('create-folder');
    setError(null);
    setSuccess(null);
    try {
      await api.post('/folders', { name, ...(currentFolder ? { parentId: currentFolder } : {}) });
      setSuccess(`Folder “${name}” was created.`);
      fetchFiles(0);
    } catch (err) {
      setError(getActionErrorMessage(err, 'Could not create folder'));
    } finally {
      setActionLoading(null);
    }
  };

  const handleRename = async (fileId, newName) => {
    if (actionLoading) return;
    setActionLoading(`rename:${fileId}`);
    setError(null);
    setSuccess(null);
    try {
      await api.patch(`/files/${fileId}`, { name: newName });
      setSuccess(`Item was renamed to “${newName}”.`);
      fetchFiles(0);
    } catch (err) {
      setError(getActionErrorMessage(err, 'Could not rename file'));
    } finally {
      setActionLoading(null);
    }
  };

  const handleDelete = async (fileId) => {
    if (actionLoading) return;
    setActionLoading(`delete:${fileId}`);
    setError(null);
    setSuccess(null);
    const item = files.find((file) => file.id === fileId);
    try {
      await api.delete(`/files/${fileId}`);
      setSuccess(`${item?.folder ? 'Folder' : 'File'} “${item?.name ?? ''}” was deleted.`);
      fetchFiles(0);
    } catch (err) {
      setError(getActionErrorMessage(err, 'Could not delete file'));
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
        {error && <div className="error-message" role="alert">{error}</div>}
        {success && <div className="success-message" role="status">{success}</div>}
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

import React, { useState, useEffect } from 'react';
import './App.css';
import FileExplorer from './components/FileExplorer';
import api from './services/api';

function App() {
  const [files, setFiles] = useState([]);
  const [currentFolder, setCurrentFolder] = useState(null);
  const [currentFolderName, setCurrentFolderName] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [backendError, setBackendError] = useState(false);

  useEffect(() => {
    fetchFiles();
  }, [currentFolder]);

  const fetchFiles = async () => {
    setLoading(true);
    setError(null);
    setBackendError(false);
    try {
      const response = await api.get('/files/list', {
        params: currentFolder ? { parentId: currentFolder } : {}
      });
      setFiles(response.data);
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
  };

  const handleCreateFile = async (name) => {
    try {
      await api.post('/files/create-file', null, {
        params: {
          name,
          ...(currentFolder ? { parentId: currentFolder } : {})
        }
      });
      fetchFiles();
    } catch (err) {
      setError('Failed to create file: ' + err.message);
    }
  };

  const handleCreateFolder = async (name) => {
    try {
      await api.post('/files/create-folder', null, {
        params: {
          name,
          ...(currentFolder ? { parentId: currentFolder } : {})
        }
      });
      fetchFiles();
    } catch (err) {
      setError('Failed to create folder: ' + err.message);
    }
  };

  const handleRename = async (fileId, newName) => {
    try {
      await api.put(`/files/${fileId}/rename`, null, {
        params: { newName: newName }
      });
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
    setCurrentFolder(folder.id);
    setCurrentFolderName(folder.name);
  };

  const handleGoBack = () => {
    setCurrentFolder(null);
    setCurrentFolderName(null);
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
            currentFolderName={currentFolderName}
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

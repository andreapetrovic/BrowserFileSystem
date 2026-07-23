import axios from 'axios';

const isDevelopment = process.env.NODE_ENV === 'development';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add request interceptor for logging
api.interceptors.request.use(
  (request) => {
    if (isDevelopment) {
      console.log('API Request:', request.method.toUpperCase(), request.url);
    }
    return request;
  },
  (error) => Promise.reject(error)
);

// Add response interceptor for logging
api.interceptors.response.use(
  (response) => {
    if (isDevelopment) {
      console.log('API Response:', response.status, response.data);
    }
    return response;
  },
  (error) => {
    if (isDevelopment) {
      console.error('API Error:', error.response?.status, error.response?.data || error.message);
    }
    return Promise.reject(error);
  }
);

export default api;

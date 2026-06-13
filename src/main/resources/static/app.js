const API_BASE = 'http://localhost:8080/api';

// Helper: returns headers with Authorization if token exists
function authHeaders() {
    const token = sessionStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
    };
}

// Redirect to login if not authenticated (used on dashboard)
function requireAuth() {
    if (!sessionStorage.getItem('token')) {
        window.location.href = 'index.html';
    }
}

function logout() {
    sessionStorage.clear();
    window.location.href = 'index.html';
}
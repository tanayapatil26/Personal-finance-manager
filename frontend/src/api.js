import axios from "axios";

const SESSION_KEY = "fm_session";

export const getSession = () => {
  try { return JSON.parse(localStorage.getItem(SESSION_KEY)); } catch { return null; }
};
export const saveSession = (session) => localStorage.setItem(SESSION_KEY, JSON.stringify(session));
export const clearSession = () => localStorage.removeItem(SESSION_KEY);

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "http://localhost:8080/api",
});

api.interceptors.request.use((config) => {
  const session = getSession();
  if (session?.token) config.headers.Authorization = `Bearer ${session.token}`;
  return config;
});

// An expired or invalid token on a protected call sends the user back to the login screen.
api.interceptors.response.use(
  (r) => r,
  (err) => {
    const isAuthCall = err.config?.url?.startsWith("/auth/");
    if (err.response?.status === 401 && !isAuthCall) {
      clearSession();
      window.dispatchEvent(new Event("auth:expired"));
    }
    return Promise.reject(err);
  }
);

export const login = (username, password) => api.post("/auth/login", { username, password }).then((r) => r.data);
export const register = (username, password) => api.post("/auth/register", { username, password }).then((r) => r.data);

export const getTransactions = (params) => api.get("/transactions", { params }).then((r) => r.data);
export const createTransaction = (body) => api.post("/transactions", body).then((r) => r.data);
export const updateTransaction = (id, body) => api.put(`/transactions/${id}`, body).then((r) => r.data);
export const deleteTransaction = (id) => api.delete(`/transactions/${id}`);
export const importLegacyFile = (file) => {
  const form = new FormData();
  form.append("file", file);
  return api.post("/transactions/import", form).then((r) => r.data);
};

export const getSummary = (month) => api.get("/summary", { params: { month } }).then((r) => r.data);
export const getCategories = (month, type = "EXPENSE") =>
  api.get("/analytics/categories", { params: { month, type } }).then((r) => r.data);
export const getMonthly = (months = 6) => api.get("/analytics/monthly", { params: { months } }).then((r) => r.data);
export const getForecast = () => api.get("/analytics/forecast").then((r) => r.data);
export const setBudget = (month, amount) => api.put("/budget", { month, amount }).then((r) => r.data);

export const errorMessage = (err) =>
  err.response?.data?.errors
    ? Object.values(err.response.data.errors).join(". ")
    : err.response?.data?.message || "Can't reach the server. Is the backend running on port 8080?";


export const getSavingsGoals = () =>
  api.get("/savings-goals").then((r) => r.data);

export const createSavingsGoal = (goal) =>
  api.post("/savings-goals", goal).then((r) => r.data);

export const updateSavingsGoal = (id, goal) =>
  api.put(`/savings-goals/${id}`, goal).then((r) => r.data);

export const deleteSavingsGoal = (id) =>
  api.delete(`/savings-goals/${id}`);

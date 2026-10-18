import { useState } from "react";
import { errorMessage, login, register, saveSession } from "./api";

export default function AuthScreen({ onAuth }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ username: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      const fn = mode === "login" ? login : register;
      const session = await fn(form.username, form.password);
      saveSession(session);
      onAuth(session);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <main className="auth-wrap">
      <form className="auth-card stack" onSubmit={submit}>
        <h1>Personal Finance Manager</h1>
        <p className="muted">
          {mode === "login" ? "Log in to see your budget and transactions." : "Create an account to start tracking."}
        </p>
        {error && <div className="alert" role="alert">{error}</div>}
        <input placeholder="Username" autoComplete="username" value={form.username}
          onChange={(e) => setForm({ ...form, username: e.target.value })} required />
        <input type="password" placeholder="Password (6+ characters)" value={form.password}
          autoComplete={mode === "login" ? "current-password" : "new-password"}
          onChange={(e) => setForm({ ...form, password: e.target.value })} required />
        <button type="submit" className="primary" disabled={busy}>
          {busy ? "Please wait..." : mode === "login" ? "Log in" : "Create account"}
        </button>
        <button type="button" onClick={() => { setMode(mode === "login" ? "register" : "login"); setError(""); }}>
          {mode === "login" ? "Need an account? Register" : "Have an account? Log in"}
        </button>
      </form>
    </main>
  );
}

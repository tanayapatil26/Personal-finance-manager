import { useState } from "react";
import {
  errorMessage,
  login,
  register,
  saveSession,
  forgotPassword,
} from "./api";

export default function AuthScreen({ onAuth }) {
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
  });
  const [resetEmail, setResetEmail] = useState("");
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");
  const [busy, setBusy] = useState(false);

  const isLogin = mode === "login";
  const isForgotPassword = mode === "forgot";

  const updateField = (field, value) => {
    setForm((previous) => ({ ...previous, [field]: value }));
  };

  const toggleMode = () => {
    setMode(isLogin ? "register" : "login");
    setError("");
    setNotice("");
  };

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    setNotice("");

    try {
      if (isForgotPassword) {
        const result = await forgotPassword(resetEmail.trim());
        setNotice(
          result.message ||
          "If an account exists for that email, a password-reset link will be sent."
        );
        return;
      }

      const session = isLogin
        ? await login(form.username, form.password)
        : await register(form.username, form.email, form.password);

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
          {isForgotPassword
            ? "Enter your registered email to request a password-reset link."
            : isLogin
              ? "Log in to see your budget and transactions."
              : "Create an account to start tracking."}
        </p>

        {error && (
          <div className="alert" role="alert">
            {error}
          </div>
        )}

        {notice && (
          <div className="alert" role="status">
            {notice}
          </div>
        )}

        {isForgotPassword ? (
          <input
            type="email"
            placeholder="Registered email address"
            autoComplete="email"
            value={resetEmail}
            onChange={(e) => setResetEmail(e.target.value)}
            maxLength={254}
            required
          />
        ) : (
          <>
            <input
              type="text"
              placeholder="Username"
              autoComplete="username"
              value={form.username}
              onChange={(e) => updateField("username", e.target.value)}
              minLength={3}
              maxLength={30}
              required
            />

            {!isLogin && (
              <input
                type="email"
                placeholder="Email address"
                autoComplete="email"
                value={form.email}
                onChange={(e) => updateField("email", e.target.value)}
                maxLength={254}
                required
              />
            )}

            <input
              type="password"
              placeholder="Password (6+ characters)"
              autoComplete={isLogin ? "current-password" : "new-password"}
              value={form.password}
              onChange={(e) => updateField("password", e.target.value)}
              minLength={6}
              maxLength={72}
              required
            />
          </>
        )}

        <button type="submit" className="primary" disabled={busy}>
          {busy
            ? "Please wait..."
            : isForgotPassword
              ? "Send reset link"
              : isLogin
                ? "Log in"
                : "Create account"}
        </button>

        {isLogin && (
          <button
            type="button"
            onClick={() => {
              setMode("forgot");
              setError("");
              setNotice("");
              setResetEmail(form.email);
            }}
            disabled={busy}
          >
            Forgot Password?
          </button>
        )}

        {isForgotPassword ? (
          <button
            type="button"
            onClick={() => {
              setMode("login");
              setError("");
              setNotice("");
            }}
            disabled={busy}
          >
            Back to login
          </button>
        ) : (
          !isForgotPassword && (
            <button type="button" onClick={toggleMode} disabled={busy}>
              {isLogin
                ? "Need an account? Register"
                : "Have an account? Log in"}
            </button>
          )
        )}
      </form>
    </main>
  );
}
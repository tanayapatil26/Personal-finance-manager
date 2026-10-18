import { useEffect, useState } from "react";
import App from "./App.jsx";
import AuthScreen from "./AuthScreen.jsx";
import { clearSession, getSession } from "./api";

export default function Root() {
  const [session, setSession] = useState(getSession());

  const logout = () => {
    clearSession();
    setSession(null);
  };

  useEffect(() => {
    window.addEventListener("auth:expired", logout);
    return () => window.removeEventListener("auth:expired", logout);
  }, []);

  return session ? <App username={session.username} onLogout={logout} /> : <AuthScreen onAuth={setSession} />;
}

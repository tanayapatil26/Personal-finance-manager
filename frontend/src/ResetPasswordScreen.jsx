import { useState } from "react";
import { errorMessage, resetPassword } from "./api";

export default function ResetPasswordScreen() {
    const token = new URLSearchParams(window.location.search).get("token");

    const [password, setPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [notice, setNotice] = useState("");
    const [busy, setBusy] = useState(false);

    const submit = async (e) => {
        e.preventDefault();
        setError("");
        setNotice("");

        if (!token) {
            setError("This reset link is missing its token. Please request a new link.");
            return;
        }

        if (password.length < 6 || password.length > 72) {
            setError("Your password must be between 6 and 72 characters.");
            return;
        }

        if (password !== confirmPassword) {
            setError("The passwords do not match.");
            return;
        }

        setBusy(true);

        try {
            const result = await resetPassword(token, password);
            setNotice(result.message || "Password reset successfully.");
            setPassword("");
            setConfirmPassword("");
        } catch (err) {
            setError(errorMessage(err));
        } finally {
            setBusy(false);
        }
    };

    return (
        <main className="auth-wrap">
            <form className="auth-card stack" onSubmit={submit}>
                <h1>Reset Password</h1>

                <p className="muted">
                    Choose a new password for your Personal Finance Manager account.
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

                <input
                    type="password"
                    placeholder="New password (6–72 characters)"
                    autoComplete="new-password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    minLength={6}
                    maxLength={72}
                    required
                />

                <input
                    type="password"
                    placeholder="Confirm new password"
                    autoComplete="new-password"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    minLength={6}
                    maxLength={72}
                    required
                />

                <button type="submit" className="primary" disabled={busy}>
                    {busy ? "Please wait..." : "Reset Password"}
                </button>

                <button
                    type="button"
                    onClick={() => {
                        window.location.href = "/";
                    }}
                    disabled={busy}
                >
                    Back to login
                </button>
            </form>
        </main>
    );
}
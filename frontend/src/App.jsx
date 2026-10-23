import { useCallback, useEffect, useState } from "react";
import {
  Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";

import {
  createTransaction, deleteTransaction, errorMessage, getCategories, getForecast,
  getMonthly, getSummary, getTransactions, importLegacyFile, setBudget, updateTransaction,
  getSavingsGoals, createSavingsGoal, updateSavingsGoal, deleteSavingsGoal,
} from "./api";


const PALETTE = ["#2F4BDB", "#0E8F7E", "#D8452B", "#E0A100", "#7A4BC4", "#2B8CBE", "#8A6D5B"];
const money = (n) =>
  Number(n ?? 0).toLocaleString("en-IN", { style: "currency", currency: "INR", maximumFractionDigits: 2 });
const today = () => new Date().toISOString().slice(0, 10);
const currentMonth = () => today().slice(0, 7);
const emptyForm = { amount: "", category: "", type: "EXPENSE", date: today(), description: "" };

export default function App({ username, onLogout }) {
  const [month, setMonth] = useState(currentMonth());
  const [summary, setSummary] = useState(null);
  const [categories, setCategories] = useState([]);
  const [monthly, setMonthly] = useState([]);
  const [forecast, setForecast] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [filters, setFilters] = useState({ category: "", type: "" });
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [budgetInput, setBudgetInput] = useState("");
  const [error, setError] = useState("");

  const [savingsGoals, setSavingsGoals] = useState([]);
  const [goalName, setGoalName] = useState("");
  const [goalTarget, setGoalTarget] = useState("");
  const [goalSaved, setGoalSaved] = useState("0");
  const [goalDate, setGoalDate] = useState("");
  const [goalError, setGoalError] = useState("");


  const load = useCallback(async () => {
    try {
      setError("");
      const [y, m] = month.split("-").map(Number);
      const lastDay = new Date(y, m, 0).getDate();
      const params = { from: `${month}-01`, to: `${month}-${String(lastDay).padStart(2, "0")}` };
      if (filters.category) params.category = filters.category;
      if (filters.type) params.type = filters.type;
      const [s, c, monthlyData, f, t] = await Promise.all([
        getSummary(month), getCategories(month), getMonthly(6), getForecast(), getTransactions(params),
      ]);
      setSummary(s); setCategories(c); setMonthly(monthlyData); setForecast(f); setTransactions(t);
    } catch (e) {
      setError(errorMessage(e));
    }
  }, [month, filters]);

  useEffect(() => { load(); }, [load]);

  const loadSavingsGoals = useCallback(async () => {
    try {
      const goals = await getSavingsGoals();
      setSavingsGoals(goals);
      setGoalError("");
    } catch (e) {
      setGoalError(errorMessage(e));
    }
  }, []);

  useEffect(() => {
    loadSavingsGoals();
  }, [loadSavingsGoals]);


  const submit = async (e) => {
    e.preventDefault();
    try {
      const body = { ...form, amount: Number(form.amount) };
      editingId ? await updateTransaction(editingId, body) : await createTransaction(body);
      setForm(emptyForm); setEditingId(null);
      load();
    } catch (err) { setError(errorMessage(err)); }
  };

  const startEdit = (t) => {
    setEditingId(t.id);
    setForm({ amount: t.amount, category: t.category, type: t.type, date: t.date, description: t.description || "" });
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const remove = async (id) => {
    if (!window.confirm("Delete this transaction?")) return;
    try { await deleteTransaction(id); load(); } catch (err) { setError(errorMessage(err)); }
  };

  const saveBudget = async (e) => {
    e.preventDefault();
    try { await setBudget(month, Number(budgetInput)); setBudgetInput(""); load(); }
    catch (err) { setError(errorMessage(err)); }
  };


  const saveGoal = async (e) => {
    e.preventDefault();
    try {
      const goal = {
        name: goalName,
        targetAmount: Number(goalTarget),
        savedAmount: Number(goalSaved),
        targetDate: goalDate || null,
      };
      await createSavingsGoal(goal);
      setGoalName("");
      setGoalTarget("");
      setGoalSaved("0");
      setGoalDate("");
      setGoalError("");
      loadSavingsGoals();
    } catch (err) {
      setGoalError(errorMessage(err));
    }
  };

  const removeGoal = async (id) => {
    if (!window.confirm("Delete this savings goal?")) return;
    try {
      await deleteSavingsGoal(id);
      loadSavingsGoals();
    } catch (err) {
      setGoalError(errorMessage(err));
    }
  };

  const saveGoalProgress = async (goal) => {
    const value = window.prompt(
      "Enter the total amount saved so far:",
      String(goal.savedAmount ?? 0)
    );
    if (value === null) return;
    const savedAmount = Number(value);
    if (!Number.isFinite(savedAmount) || savedAmount < 0) {
      setGoalError("Enter a valid amount that is zero or more.");
      return;
    }
    try {
      await updateSavingsGoal(goal.id, {
        name: goal.name,
        targetAmount: Number(goal.targetAmount),
        savedAmount,
        targetDate: goal.targetDate || null,
      });
      loadSavingsGoals();
      setGoalError("");
    } catch (err) {
      setGoalError(errorMessage(err));
    }
  };


  const importFile = async (e) => {
    const file = e.target.files[0];
    if (!file) return;
    try { const r = await importLegacyFile(file); setError(""); alert(`Imported ${r.imported} transactions.`); load(); }
    catch (err) { setError(errorMessage(err)); }
    e.target.value = "";
  };

  const budget = summary?.budget;
  const pct = budget ? Math.min(100, (Number(summary.totalExpenses) / Number(budget)) * 100) : 0;

  return (
    <div className="page">
      <header className="topbar">
        <h1>Personal Finance Manager</h1>
        <label className="month-picker">
          Month
          <input type="month" value={month} onChange={(e) => setMonth(e.target.value)} />
        </label>
        <div className="user-box">
          <span>{username}</span>
          <button onClick={onLogout}>Log out</button>
        </div>
      </header>

      {error && <div className="alert" role="alert">{error}</div>}

      <section className="budget-panel" aria-label="Monthly budget">
        <div className="budget-head">
          <div>
            <p className="label">Spent this month</p>
            <p className="big">{money(summary?.totalExpenses)}</p>
          </div>
          <div className="budget-right">
            <p className="label">{budget ? `Budget ${money(budget)}` : "No budget set for this month"}</p>
            {budget && (
              <p className={summary.budgetExceeded ? "status over" : "status ok"}>
                {summary.budgetExceeded
                  ? `Over budget by ${money(Math.abs(summary.remainingBudget))}`
                  : `${money(summary.remainingBudget)} left`}
              </p>
            )}
          </div>
        </div>
        <div className="gauge" role="progressbar" aria-valuenow={Math.round(pct)} aria-valuemin={0} aria-valuemax={100}>
          <div className={`gauge-fill ${summary?.budgetExceeded ? "over" : ""}`} style={{ width: `${pct}%` }} />
        </div>
        <form className="inline-form" onSubmit={saveBudget}>
          <input type="number" min="1" step="0.01" placeholder="Set monthly budget" value={budgetInput}
            onChange={(e) => setBudgetInput(e.target.value)} required />
          <button type="submit">Save budget</button>
        </form>
        <dl className="totals">
          <div><dt>Income</dt><dd className="income">{money(summary?.totalIncome)}</dd></div>
          <div><dt>Expenses</dt><dd className="expense">{money(summary?.totalExpenses)}</dd></div>
          <div><dt>Balance</dt><dd>{money(summary?.balance)}</dd></div>
        </dl>
      </section>

      <div className="grid">
        <section className="panel">
          <h2>{editingId ? "Edit transaction" : "Add transaction"}</h2>
          <form className="stack" onSubmit={submit}>
            <div className="seg">
              {["EXPENSE", "INCOME"].map((t) => (
                <button type="button" key={t} className={form.type === t ? "active" : ""}
                  onClick={() => setForm({ ...form, type: t })}>
                  {t === "EXPENSE" ? "Expense" : "Income"}
                </button>
              ))}
            </div>
            <input type="number" min="0.01" step="0.01" placeholder="Amount" value={form.amount}
              onChange={(e) => setForm({ ...form, amount: e.target.value })} required />
            <input placeholder="Category (e.g. Food, Rent)" value={form.category}
              onChange={(e) => setForm({ ...form, category: e.target.value })} required />
            <input type="date" value={form.date} onChange={(e) => setForm({ ...form, date: e.target.value })} />
            <input placeholder="Note (optional)" value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })} />
            <button type="submit" className="primary">{editingId ? "Save changes" : "Add transaction"}</button>
            {editingId && (
              <button type="button" onClick={() => { setEditingId(null); setForm(emptyForm); }}>Cancel edit</button>
            )}
          </form>
          

<label className="import">
  Import transactions from a text file (.txt)
  <input type="file" accept=".txt" onChange={importFile} />
</label>
</section>

<label className="import">
  Import transactions from a text file (.txt)
  <input type="file" accept=".txt" onChange={importFile} />
</label>
</section>

</section>

        </section>

        <section className="panel">
          <h2>Where the money went</h2>
          {categories.length === 0 ? (
            <p className="empty">No expenses this month. Add one to see the breakdown.</p>
          ) : (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={categories.map((c) => ({ name: c.category, value: Number(c.total) }))}
                  dataKey="value" nameKey="name" innerRadius={55} outerRadius={95} paddingAngle={2}>
                  {categories.map((_, i) => <Cell key={i} fill={PALETTE[i % PALETTE.length]} />)}
                </Pie>
                <Tooltip formatter={(v) => money(v)} />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </section>

        <section className="panel wide">
          <h2>Last 6 months</h2>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={monthly.map((m) => ({ ...m, income: Number(m.income), expense: Number(m.expense) }))}>
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip formatter={(v) => money(v)} />
              <Legend />
              <Bar dataKey="income" fill="#0E8F7E" radius={[3, 3, 0, 0]} />
              <Bar dataKey="expense" fill="#D8452B" radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
          {forecast && (
            <p className="forecast">
              Forecast for {forecast.nextMonth}: <strong>{money(forecast.predictedExpense)}</strong> in expenses
              (spending is {forecast.trend}). Based on {forecast.method.toLowerCase()}.
            </p>
          )}
        </section>
      </div>


      <section className="panel wide">
        <h2>Savings Goals</h2>
        <p className="empty">Set a target, track your savings, and work toward your goal.</p>

        <form className="stack" onSubmit={saveGoal}>
          <input
            placeholder="Goal name (e.g. New laptop)"
            value={goalName}
            onChange={(e) => setGoalName(e.target.value)}
            required
          />
          <input
            type="number"
            min="0.01"
            step="0.01"
            placeholder="Target amount (₹)"
            value={goalTarget}
            onChange={(e) => setGoalTarget(e.target.value)}
            required
          />
          <input
            type="number"
            min="0"
            step="0.01"
            placeholder="Already saved (₹)"
            value={goalSaved}
            onChange={(e) => setGoalSaved(e.target.value)}
            required
          />
          <label>
            Target date (optional)
            <input
              type="date"
              value={goalDate}
              onChange={(e) => setGoalDate(e.target.value)}
            />
          </label>
          <button type="submit" className="primary">Add savings goal</button>
        </form>

        {goalError && <p className="expense">{goalError}</p>}

        {savingsGoals.length === 0 ? (
          <p className="empty">No savings goals yet. Add your first goal above!</p>
        ) : (
          <div className="stack">
            {savingsGoals.map((goal) => {
              const target = Number(goal.targetAmount);
              const saved = Number(goal.savedAmount ?? 0);
              const progress = target > 0
                ? Math.min(100, (saved / target) * 100)
                : 0;

              return (
                <div className="panel" key={goal.id}>
                  <h3>{goal.name}</h3>
                  <p>
                    Saved: <strong>{money(saved)}</strong> of {money(target)}
                  </p>
                  <progress value={progress} max="100" style={{ width: "100%" }} />
                  <p>{progress.toFixed(1)}% complete</p>
                  {goal.targetDate && <p>Target date: {goal.targetDate}</p>}
                  <div className="actions">
                    <button type="button" onClick={() => saveGoalProgress(goal)}>
                      Update progress
                    </button>
                    <button
                      type="button"
                      className="danger"
                      onClick={() => removeGoal(goal.id)}
                    >
                      Delete
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </section>


      <section className="panel">
        <div className="table-head">
          <h2>Transactions</h2>
          <div className="filters">
            <input placeholder="Filter by category" value={filters.category}
              onChange={(e) => setFilters({ ...filters, category: e.target.value })} />
            <select value={filters.type} onChange={(e) => setFilters({ ...filters, type: e.target.value })}>
              <option value="">All types</option>
              <option value="INCOME">Income</option>
              <option value="EXPENSE">Expense</option>
            </select>
          </div>
        </div>
        {transactions.length === 0 ? (
          <p className="empty">No transactions match. Change the month or filters, or add a transaction.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Date</th><th>Category</th><th>Note</th><th className="num">Amount</th><th /></tr>
              </thead>
              <tbody>
                {transactions.map((t) => (
                  <tr key={t.id}>
                    <td>{t.date}</td>
                    <td>{t.category}</td>
                    <td>{t.description}</td>
                    <td className={`num ${t.type === "INCOME" ? "income" : "expense"}`}>
                      {t.type === "INCOME" ? "+" : "-"}{money(t.amount)}
                    </td>
                    <td className="actions">
                      <button onClick={() => startEdit(t)}>Edit</button>
                      <button className="danger" onClick={() => remove(t.id)}>Delete</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

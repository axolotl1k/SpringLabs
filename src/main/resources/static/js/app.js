document.addEventListener("DOMContentLoaded", function () {
    const root = document.documentElement;
    const toggle = document.getElementById("themeSwitch");

    const savedTheme = localStorage.getItem("theme") || "light";

    if (savedTheme === "dark") {
        root.classList.add("dark");
    } else {
        root.classList.remove("dark");
    }

    if (toggle) {
        toggle.checked = savedTheme === "dark";

        toggle.addEventListener("change", () => {
            const newTheme = toggle.checked ? "dark" : "light";
            localStorage.setItem("theme", newTheme);

            if (newTheme === "dark") {
                root.classList.add("dark");
            } else {
                root.classList.remove("dark");
            }
        });
    }

    const presentCheckbox = document.getElementById("present-checkbox");
    const gradeInput = document.getElementById("grade-input");
    const presenceLabel =
        presentCheckbox?.closest(".switch")?.querySelector(".switch-label");

    function updatePresenceLabel() {
        if (!presentCheckbox || !presenceLabel) return;
        presenceLabel.textContent = presentCheckbox.checked ? "Присутній" : "Відсутній";
    }

    function toggleGradeInput() {
        if (!presentCheckbox || !gradeInput) return;
        if (presentCheckbox.checked) {
            gradeInput.disabled = false;
        } else {
            gradeInput.disabled = true;
            gradeInput.value = "";
        }
    }

    if (presentCheckbox) {
        updatePresenceLabel();
        toggleGradeInput();
        presentCheckbox.addEventListener("change", () => {
            updatePresenceLabel();
            toggleGradeInput();
        });
    }
});

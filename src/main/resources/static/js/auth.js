/**
 * Handles client-side submission of the registration form.
 * Posts to POST /api/auth/register as JSON and renders either a
 * success message (redirecting to /login) or field-level validation
 * errors returned by GlobalExceptionHandler.
 */
document.addEventListener('DOMContentLoaded', function () {
    const form = document.getElementById('registerForm');
    if (!form) {
        return;
    }

    const alertBox = document.getElementById('registerAlert');

    form.addEventListener('submit', function (event) {
        event.preventDefault();
        clearFieldErrors(form);
        hideAlert(alertBox);

        const payload = {
            fullName: form.fullName.value.trim(),
            email: form.email.value.trim(),
            password: form.password.value,
            department: form.department.value.trim(),
            yearOfStudy: form.yearOfStudy.value ? parseInt(form.yearOfStudy.value, 10) : null
        };

        fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        })
            .then(function (response) {
                return response.json().then(function (body) {
                    return { status: response.status, body: body };
                });
            })
            .then(function (result) {
                if (result.status === 201 && result.body.success) {
                    showAlert(alertBox, 'success', 'Registration successful! Redirecting to login...');
                    setTimeout(function () {
                        window.location.href = '/login';
                    }, 1200);
                } else {
                    showAlert(alertBox, 'danger', result.body.message || 'Registration failed. Please check your details.');
                }
            })
            .catch(function () {
                showAlert(alertBox, 'danger', 'Unable to reach the server. Please try again.');
            });
    });

    function showAlert(box, type, message) {
        box.className = 'alert alert-' + type;
        box.textContent = message;
        box.classList.remove('d-none');
    }

    function hideAlert(box) {
        box.classList.add('d-none');
    }

    function clearFieldErrors(formEl) {
        formEl.querySelectorAll('.invalid-feedback').forEach(function (el) {
            el.textContent = '';
        });
        formEl.querySelectorAll('.is-invalid').forEach(function (el) {
            el.classList.remove('is-invalid');
        });
    }
});

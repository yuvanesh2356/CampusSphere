/**
 * Marketplace form enhancement: shows a live preview of the selected
 * image file before the form is submitted. Purely a UX nicety - the
 * form still works correctly (server-side validation and storage via
 * FileStorageService) if JavaScript is disabled.
 */
document.addEventListener('DOMContentLoaded', function () {
    const imageInput = document.getElementById('image');
    const previewImg = document.getElementById('mktImagePreview');

    if (!imageInput || !previewImg) {
        return;
    }

    imageInput.addEventListener('change', function () {
        const file = imageInput.files && imageInput.files[0];
        if (!file) {
            return;
        }

        const reader = new FileReader();
        reader.onload = function (event) {
            previewImg.src = event.target.result;
            previewImg.classList.remove('d-none');
        };
        reader.readAsDataURL(file);
    });
});

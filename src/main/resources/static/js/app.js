/**
 * Toggles the sidebar drawer open/closed on mobile (below the md
 * breakpoint, the sidebar is off-canvas by default - see .cs-sidebar
 * and .cs-sidebar-open in style.css). Included once per authenticated
 * app page, alongside fragments/topbar.html which renders the toggle
 * button this listens for.
 */
document.addEventListener('DOMContentLoaded', function () {
    var toggle = document.getElementById('csSidebarToggle');
    var sidebar = document.getElementById('csSidebar');

    if (toggle && sidebar) {
        toggle.addEventListener('click', function () {
            sidebar.classList.toggle('cs-sidebar-open');
        });
    }

    // Clicking outside an open mobile sidebar closes it again.
    document.addEventListener('click', function (event) {
        if (!sidebar || !sidebar.classList.contains('cs-sidebar-open')) {
            return;
        }
        var clickedInsideSidebar = sidebar.contains(event.target);
        var clickedToggle = toggle && toggle.contains(event.target);
        if (!clickedInsideSidebar && !clickedToggle) {
            sidebar.classList.remove('cs-sidebar-open');
        }
    });
});

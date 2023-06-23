document.getElementById("app_navbar__menu_button").addEventListener("click", function() {
    document.getElementById("app_sidenav").classList.toggle("active");
});

document.getElementById("app_sidenav__blur").addEventListener("click", () => {
    document.getElementById("app_sidenav").classList.remove("active");
});
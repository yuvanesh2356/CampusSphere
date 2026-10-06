document.addEventListener("DOMContentLoaded", function () {

    /* =========================================================
       HERO CAROUSEL
       ========================================================= */

    const slides = Array.from(
        document.querySelectorAll(".dashboard-slide")
    );

    const dots = Array.from(
        document.querySelectorAll(".hero-dot")
    );

    const previousButton =
        document.querySelector(".hero-arrow-prev");

    const nextButton =
        document.querySelector(".hero-arrow-next");

    if (!slides.length) {
        return;
    }

    let currentSlide = 0;
    let autoPlayTimer = null;
    let isPaused = false;


    function showSlide(index) {

        if (index < 0) {
            index = slides.length - 1;
        }

        if (index >= slides.length) {
            index = 0;
        }

        currentSlide = index;


        slides.forEach(function (slide, i) {

            slide.classList.toggle(
                "active",
                i === currentSlide
            );

        });


        dots.forEach(function (dot, i) {

            dot.classList.toggle(
                "active",
                i === currentSlide
            );

        });

    }


    function nextSlide() {
        showSlide(currentSlide + 1);
    }


    function previousSlide() {
        showSlide(currentSlide - 1);
    }


    function startAutoPlay() {

        stopAutoPlay();

        autoPlayTimer = setInterval(function () {

            if (!isPaused) {
                nextSlide();
            }

        }, 4500);

    }


    function stopAutoPlay() {

        if (autoPlayTimer) {

            clearInterval(autoPlayTimer);

            autoPlayTimer = null;
        }

    }


    if (nextButton) {

        nextButton.addEventListener(
            "click",
            function () {

                nextSlide();
                startAutoPlay();

            }
        );

    }


    if (previousButton) {

        previousButton.addEventListener(
            "click",
            function () {

                previousSlide();
                startAutoPlay();

            }
        );

    }


    dots.forEach(function (dot, index) {

        dot.addEventListener(
            "click",
            function () {

                showSlide(index);
                startAutoPlay();

            }
        );

    });


    const hero =
        document.querySelector(".dashboard-hero");


    if (hero) {

        hero.addEventListener(
            "mouseenter",
            function () {

                isPaused = true;

            }
        );


        hero.addEventListener(
            "mouseleave",
            function () {

                isPaused = false;

            }
        );


        hero.addEventListener(
            "focusin",
            function () {

                isPaused = true;

            }
        );


        hero.addEventListener(
            "focusout",
            function () {

                isPaused = false;

            }
        );

    }


    /* =========================================================
       KEYBOARD CONTROL
       ========================================================= */

    document.addEventListener(
        "keydown",
        function (event) {

            if (event.key === "ArrowRight") {

                nextSlide();
                startAutoPlay();

            }

            if (event.key === "ArrowLeft") {

                previousSlide();
                startAutoPlay();

            }

        }
    );


    showSlide(0);
    startAutoPlay();


    /* =========================================================
       ANIMATED STATISTICS
       ========================================================= */

    const counters =
        document.querySelectorAll(".dashboard-counter");


    function animateCounter(element) {

        const target =
            parseInt(
                element.textContent.trim(),
                10
            );


        if (isNaN(target)) {
            return;
        }


        if (
            window.matchMedia(
                "(prefers-reduced-motion: reduce)"
            ).matches
        ) {
            return;
        }


        element.textContent = "0";


        const duration = 900;

        const startTime = performance.now();


        function updateCounter(currentTime) {

            const elapsed =
                currentTime - startTime;

            const progress =
                Math.min(
                    elapsed / duration,
                    1
                );


            /* Smooth ease-out */

            const eased =
                1 -
                Math.pow(
                    1 - progress,
                    3
                );


            const currentValue =
                Math.floor(
                    target * eased
                );


            element.textContent =
                currentValue;


            if (progress < 1) {

                requestAnimationFrame(
                    updateCounter
                );

            } else {

                element.textContent =
                    target;

            }

        }


        requestAnimationFrame(
            updateCounter
        );

    }


    counters.forEach(function (counter, index) {

        setTimeout(
            function () {
                animateCounter(counter);
            },
            index * 100
        );

    });

});
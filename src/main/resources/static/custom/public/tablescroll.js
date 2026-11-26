// Floating horizontal scrollbar for wide tables (see tablescroll.css). A long table's own scrollbar sits below
// all its rows, so a synced copy (.ts-fbar right after .ts-scroll) sticks to the bottom of the screen while the
// table is wider than its box and runs below the screen.
(function () {
    function wire(box, bar) {
        var inner = bar.firstElementChild, table = box.querySelector('table');
        function update() {
            var wide = box.scrollWidth > box.clientWidth + 1;
            var below = box.getBoundingClientRect().bottom > (window.innerHeight || document.documentElement.clientHeight);
            bar.classList.toggle('ts-off', !(wide && below));
            inner.style.width = box.scrollWidth + 'px';
            if (Math.abs(bar.scrollLeft - box.scrollLeft) > 1) bar.scrollLeft = box.scrollLeft;
        }
        // copy the position across only when it differs, so the echo from the other side stops here
        function follow(from, to) {
            return function () {
                if (Math.abs(to.scrollLeft - from.scrollLeft) > 1) to.scrollLeft = from.scrollLeft;
            };
        }
        box.addEventListener('scroll', follow(box, bar), { passive: true });
        bar.addEventListener('scroll', follow(bar, box), { passive: true });
        window.addEventListener('resize', update);
        window.addEventListener('scroll', update, { passive: true });
        if (window.ResizeObserver) { new ResizeObserver(update).observe(table || box); new ResizeObserver(update).observe(box); }
        else setInterval(update, 1000);
        update();
    }
    function init() {
        var boxes = document.querySelectorAll('.ts-scroll');
        for (var i = 0; i < boxes.length; i++) {
            var bar = boxes[i].nextElementSibling;
            if (bar && bar.classList.contains('ts-fbar') && bar.firstElementChild) wire(boxes[i], bar);
        }
    }
    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init); else init();
})();

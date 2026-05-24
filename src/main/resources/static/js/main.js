document.addEventListener("DOMContentLoaded", () => {

    // VALIDACIÓN DBF
    const form = document.getElementById("formProceso");
    if (form) {

        form.addEventListener("submit", (e) => {

            const files = document.querySelectorAll(".dbf-file");

            for (const fileInput of files) {

                const file = fileInput.files[0];

                if (!file.name.toLowerCase().endsWith(".dbf")) {

                    e.preventDefault();

                    alert(
                        "Solo se permiten archivos DBF"
                    );

                    return;
                }
            }
        });
    }

    // CONFIGURACIÓN PUNTAJES
    const guardarConfig =
        document.getElementById("guardarConfig");

    if (guardarConfig) {

        guardarConfig.addEventListener("click", () => {

            document.getElementById(
                "puntajeCorrecta"
            ).value = document.getElementById(
                "inputCorrecta"
            ).value;

            document.getElementById(
                "puntajeIncorrecta"
            ).value = document.getElementById(
                "inputIncorrecta"
            ).value;

            document.getElementById(
                "puntajeBlanca"
            ).value = document.getElementById(
                "inputBlanca"
            ).value;

            alert(
                "Configuración guardada"
            );
        });
    }

    // MODIFICAR MOTIVO DE ANULACIÓN
    document.querySelectorAll(
        ".btn-editar-motivo"
    ).forEach(btn => {

        btn.addEventListener("click", () => {

            const modal =
                btn.closest(".modal");

            const textarea =
                modal.querySelector(
                    ".motivo-textarea"
                );

            const guardarBtn =
                modal.querySelector(
                    ".btn-guardar-motivo"
                );

            textarea.removeAttribute(
                "readonly"
            );

            textarea.focus();

            textarea.classList.add(
                "border-primary"
            );

            guardarBtn.classList.remove(
                "d-none"
            );

            btn.classList.add(
                "d-none"
            );
        });
    });

    // REVISIÓN DE EXAMENES
    document.querySelectorAll(
        ".btn-mostrar-anulacion"
    ).forEach(btn => {

        btn.addEventListener(
            "click",
            () => {
                const modalBody =
                    btn.closest(".modal-body");
                modalBody.querySelector(
                    ".revision-anulacion-panel"
                ).classList.remove("d-none");
            }
        );
    });

    document.querySelectorAll(
        ".btn-cancelar-anulacion"
    ).forEach(btn => {
        btn.addEventListener(
            "click",
            () => {
                const panel =
                    btn.closest(
                        ".revision-anulacion-panel"
                    );
                panel.classList.add("d-none");
            }
        );
    });

    // TABLA CON PAGINACION DINAMICA
    const tabla = document.getElementById(
        "tablaExamenes"
    );

    if (tabla) {

        const tbody =
            tabla.querySelector("tbody");

        let filasOriginales =
            Array.from(
                tbody.querySelectorAll("tr")
            );

        let filasFiltradas =
            [...filasOriginales];

        let paginaActual = 1;

        const filasPorPagina = 10;

        const buscador =
            document.getElementById(
                "buscadorCodigo"
            );

        const selectOrden =
            document.getElementById(
                "ordenPuntaje"
            );

        const filtroCarrera =
            document.getElementById(
                "filtroCarrera"
            );

        const filtroEstado =
            document.getElementById(
                "filtroEstado"
            );

        const btnAnterior =
            document.getElementById(
                "btnAnterior"
            );

        const btnSiguiente =
            document.getElementById(
                "btnSiguiente"
            );

        const inputPagina =
            document.getElementById(
                "inputPagina"
            );

        const infoPaginacion =
            document.getElementById(
                "infoPaginacion"
            );

        // RENDER PAGINACIÓN
        function renderTabla() {

            tbody.innerHTML = "";

            const inicio =
                (paginaActual - 1)
                * filasPorPagina;

            const fin =
                inicio + filasPorPagina;

            const filasPagina =
                filasFiltradas.slice(
                    inicio,
                    fin
                );

            filasPagina.forEach(fila => {

                tbody.appendChild(fila);
            });

            const totalPaginas =
                Math.ceil(
                    filasFiltradas.length
                    / filasPorPagina
                ) || 1;

            infoPaginacion.textContent =
                `Página ${paginaActual} de ${totalPaginas}`;

            inputPagina.value =
                paginaActual;

            btnAnterior.disabled =
                paginaActual === 1;

            btnSiguiente.disabled =
                paginaActual === totalPaginas;
        }

        // FILTRAR
        function aplicarFiltros() {

            const textoBusqueda =
                buscador.value
                    .toLowerCase();

            const carreraSeleccionada =
                filtroCarrera.value
                    .toLowerCase();

            const estadoSeleccionado =
                filtroEstado.value;

            filasFiltradas =
                filasOriginales.filter(fila => {

                    const codigo =
                        fila.children[2]
                            .textContent
                            .toLowerCase();

                    const carrera =
                        fila.children[0]
                            .textContent
                            .toLowerCase();

                    const anulado =
                        fila.dataset.anulado === "true";

                    const revision =
                        fila.dataset.revision === "true";

                    const coincideCodigo =
                        codigo.includes(
                            textoBusqueda
                        );

                    const coincideCarrera =
                        carreraSeleccionada === ""
                        ||
                        carrera.includes(
                            carreraSeleccionada
                        );

                    let coincideEstado = true;

                    if (estadoSeleccionado === "valido") {
                        coincideEstado =
                            !anulado && !revision;
                    }
                    else if (estadoSeleccionado === "anulado") {
                        coincideEstado =
                            anulado && !revision;
                    }
                    else if (estadoSeleccionado === "revision") {
                        coincideEstado =
                            revision;
                    }

                    return coincideCodigo
                        &&
                        coincideCarrera
                        &&
                        coincideEstado;
                });

            ordenarTabla();
            paginaActual = 1;
            renderTabla();
        }

        // ORDENAR
        function ordenarTabla() {

            filasFiltradas.sort((a, b) => {

                const puntajeA =
                    parseFloat(
                        a.querySelector(
                            ".puntaje"
                        ).textContent
                    );

                const puntajeB =
                    parseFloat(
                        b.querySelector(
                            ".puntaje"
                        ).textContent
                    );

                return selectOrden.value === "asc"
                    ? puntajeA - puntajeB
                    : puntajeB - puntajeA;
            });
        }

        // EVENTOS
        buscador.addEventListener(
            "keyup",
            aplicarFiltros
        );

        selectOrden.addEventListener(
            "change",
            () => {
                ordenarTabla();
                renderTabla();
            }
        );

        filtroCarrera.addEventListener(
            "change",
            aplicarFiltros
        );

        filtroEstado.addEventListener(
            "change",
            aplicarFiltros
        );

        btnAnterior.addEventListener(
            "click",
            () => {
                if (paginaActual > 1) {
                    paginaActual--;
                    renderTabla();
                }
            }
        );

        btnSiguiente.addEventListener(
            "click",
            () => {
                const totalPaginas =
                    Math.ceil(
                        filasFiltradas.length
                        / filasPorPagina
                    );
                if (paginaActual < totalPaginas) {
                    paginaActual++;
                    renderTabla();
                }
            }
        );

        inputPagina.addEventListener(
            "keydown",
            (e) => {

                if (e.key === "Enter") {
                    const totalPaginas =
                        Math.ceil(
                            filasFiltradas.length
                            / filasPorPagina
                        );
                    let pagina =
                        parseInt(
                            inputPagina.value
                        );
                    if (isNaN(pagina)) {
                        pagina = 1;
                    }
                    pagina = Math.max(
                        1,
                        Math.min(
                            pagina,
                            totalPaginas
                        )
                    );
                    paginaActual = pagina;
                    renderTabla();
                }
            }
        );

        // INICIALIZAR
        ordenarTabla();
        renderTabla();
    }

    // PREVIEW DE EVIDENCIAS
    document.querySelectorAll(
        ".evidencia-input"
    ).forEach(input => {

        input.addEventListener(
            "change",
            function () {
                const previewContainer =
                    this.closest(".modal-body")
                        .querySelector(
                            ".preview-evidencias"
                        );

                previewContainer.innerHTML = "";

                Array.from(this.files)
                    .forEach(file => {

                        const reader =
                            new FileReader();

                        reader.onload = e => {

                            const wrapper = document.createElement("div");
                            wrapper.classList.add("preview-card");

                            wrapper.innerHTML = `
                            <img src="${e.target.result}"
                                 class="preview-img">
                            <div class="preview-name">
                                ${file.name}
                            </div>
                        `;

                            previewContainer.appendChild(wrapper);
                        };

                        reader.readAsDataURL(file);
                    });
            }
        );
    });
});
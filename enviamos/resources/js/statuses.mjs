export default class Statuses {
  static #table
  static #modal
  static #currentOption
  static #form
  static #listStatuses
  static #optStatuses
  static #formEntry
  static #shipmentType
  static #guideNumber
  static #estadosTabla
  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Statuses.init()')
  }

  static async init() {
    try {
      // Cargar el formulario y agregarlo al <main>
      Statuses.#form = await Helpers.fetchText('./resources/html/statuses.html')
      document.querySelector('main').innerHTML = Statuses.#form
      Statuses.#formEntry = await Helpers.fetchText('./resources/html/statusesForm.html')

      // Iniciar el reloj para la date y hora actuales
      Statuses.startClock()

      // Asociar el evento de búsqueda
      document.querySelector('#search-shipment').addEventListener('click', async event => {
        event.preventDefault()
        const shipmentType = document.querySelector('#shipment-type').value
        const guideNumber = document.querySelector('#numGuide').value.trim()
        Statuses.#shipmentType = shipmentType
        Statuses.#guideNumber = guideNumber

        if (!shipmentType || !guideNumber) {
          Toast.show({
            title: 'Error',
            message: 'Please, select a shipment type and enter the number guide.',
            mode: 'warning',
          })
          return
        }
        if (shipmentType === 'select a shipment') {
          Toast.show({
            title: 'Error',
            message: 'Please, select a shipment type.',
            mode: 'warning',
          })
          return
        }

        //Obtener la lista de estados
        const response2 = await Helpers.fetchJSON(`${urlAPI}/delivery/statuses`)

        if (response2.message != 'ok') {
          throw new Error(response.message)
        }
        Statuses.#listStatuses = response2.data

        // Hacer la solicitud HTTP
        const response = await Helpers.fetchJSON(`${urlAPI}/${shipmentType}/id/${guideNumber}`)
        if (response.message != 'ok') {
          Toast.show({
            title: 'Error',
            message: response.message,
          })
        }
        console.log(response)

        Statuses.#estadosTabla = response.data.statuses

        // Crear la tabla con los datos de la guía
        Statuses.createTable(response, guideNumber)

        Statuses.#optStatuses = Helpers.toOptionList({
          items: response2.data,
          value: 'key',
          text: 'value',
          firstOption: 'Select an status',
        })
      })
    } catch (error) {
      Toast.show({ title: 'Statuses', message: 'Failed to load information', mode: 'danger', error })
    }
  }
  static async createTable(response, guideNumber) {
    if (Statuses.#table) {
      Statuses.#table.destroy()
    }
    //Para el caso de que no se encuentre la guía, mostrar un alert de boostrap
    const info = response.data
    const statuses = info.statuses

    if (response.message != 'ok') {
      document.querySelector('#div-info-container').innerHTML = `
     <!-- Espacio vacío -->
      <div class="row mb-3"></div>
      <!-- Titulo-->
      <div><strong><h5>Information of the shipment</h5></strong></div>
    <div class="alert alert-dismissible alert-warning">
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
      <h4 class="alert-heading">Warning!</h4>
      <p class="mb-0">No shipment found with number of guide ${guideNumber}<a href="#" class="alert-link"></a></p>
    </div>`
      return
    }
    const infoHTML = `
    <!-- Titulo-->
    <div class="p-2 w-full">
    <div><strong><h5>Information of the shipment</h5></strong></div>
    <div><strong>Sender: </strong> ${info.sender.name} - ${info.sender.address} - ${info.sender.city}</div>
    <div><strong>Addressee: </strong> ${info.addressee.name} - ${info.addressee.address} - ${info.addressee.city}</div>
    <div><strong>Says to contain: </strong> ${info.content}</div>
    <div><strong>Value of the shipment: </strong> ${info.value}</div> 
    </div>
    <div id="table-container" class="m-1"></div>
    `
    //Agregar la información de la guía al div contenedor de la informacion y la tabla
    document.querySelector('#div-info-container').innerHTML = infoHTML

    Statuses.#table = new Tabulator('#table-container', {
      height: 'auto', // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
      data: info.statuses,
      layout: 'fitDataStretch', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
      columns: [
        // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
        {
          formatter: function (cell) {
            let length = statuses.length - 1 // Índice del último estado
            let lastState = statuses[length] // Último estado de la lista
            console.log(cell.getRow().getData().deliveryStatus)
            let estado = Statuses.#estadosTabla
            estado = estado[length]
            console.log(estado)

            // Comparar el estado actual de la fila con el último estado
            if (cell.getRow().getData().deliveryStatus === estado.deliveryStatus) {
              return `<button id="delete-row" class="border-0 bg-transparent" data-bs-toggle="tooltip" title="Delete">
                        ${icons.delete}
                    </button>`
            } else {
              return `<button id="delete-row" class="border-0 bg-transparent" data-bs-toggle="tooltip" title="Delete" style="visibility:hidden" disabled>
            ${icons.delete}
        </button>` // No mostrar botón si no es el último estado
            }
          },
          width: 40,
          hozAlign: 'center',
          cellClick: function (e, cell) {
            let button = e.target.closest('#delete-row')
            if (button && !button.hasAttribute('disabled')) {
              Statuses.#deleteRowClick(e, cell)
            }
          },
        },
        {
          title: 'Date and time',
          field: 'dateTime',
          width: 400,
          formatter: cell => {
            const celda = cell.getValue()
            // Formato personalizado de la fecha y hora, en español
            const formatoPersonalizado = "hh:mm a 'of' cccc d',' LLLL',' yyyy"
            let date = DateTime.fromISO(celda).toFormat(formatoPersonalizado, { locale: 'en' })
            return date
          },
        },
        {
          title: 'Status',
          field: 'deliveryStatus',
          formatter: function (cell) {
            const celda = cell.getValue()
            //A partir de la lista con todos los estados, se busca el estado que corresponde a la celda y se retorna el valor
            const estado = Statuses.#listStatuses.find(item => item.key === celda)
            console.log(estado.value)

            return estado.value
          },
        },
      ],
      responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
      initialSort: [
        // establecer el ordenamiento inicial de los datos
        { column: 'dateTime', dir: 'asc' },
      ],
      columnDefaults: {
        tooltip: true, //show tool tips on cells
      },

      // mostrar al final de la tabla un botón para agregar registros
      footerElement: `<div class="container-fluid d-flex justify-content-end p-0">${addRowButton}</div>`,
    })
    Statuses.#table.on('tableBuilt', () => document.querySelector('#add-row').addEventListener('click', Statuses.#addRow))
  }

  static async startClock() {
    const dateInput = document.querySelector('#current-date')

    if (!dateInput) {
      console.warn('The current date and time field was not found.')
      return
    }

    const updateDateTime = () => {
      const now = DateTime.now()

      // Formatear now a 'dd/MM/yyyy , HH:mm:ss'
      const formattedDate = now.toFormat('dd/MM/yyyy , HH:mm:ss')
      // Actualizar el campo con date y hora
      dateInput.value = `${formattedDate}`
    }
    // Actualizar inmediatamente y luego cada segundo
    updateDateTime()
    //setInterval(func, delay, arg0, arg1, /* ..., */ argN); En este caso solo se le manda el callback que es una funcion que se ejecutara cada 1000 ms(delay)
    setInterval(updateDateTime, 1000)
  }
  static async #addRow() {
    Statuses.#currentOption = 'add'
    Statuses.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Entry of statuses</h5>',
      content: Statuses.#formEntry,
      buttons: [
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Statuses.#add() },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Statuses.#modal.close() },
      ],
      doSomething: Statuses.#displayDataOnForm,
    })
    Statuses.#modal.show()
  }

  static async #add() {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-statusesForm', Statuses.#otherValidations)) {
        return
      }

      // obtener del formulario el objeto con los datos que se envían a la solicitud PATCH
      const body = Statuses.#getFormData()
      // enviar la solicitud de creación con los datos del formulario, con la peticion correcta(update)
      let response = await Helpers.fetchJSON(`${urlAPI}/${Statuses.#shipmentType}/${Statuses.#guideNumber}`, {
        method: 'PATCH',
        body,
      })

      if (response.message === 'ok') {
        // let longitud = response.data.statuses.length
        // Statuses.#table.addRow(response.data.statuses[longitud - 1]) // agregar el estado a la tabla
        // Statuses.#modal.remove()
        const response = await Helpers.fetchJSON(`${urlAPI}/${Statuses.#shipmentType}/id/${Statuses.#guideNumber}`)
        Statuses.#estadosTabla = response.data.statuses
        // Crear la tabla con los datos de la guía
        Statuses.createTable(response, Statuses.#guideNumber)

        Statuses.#modal.remove()
        Toast.show({ message: 'Succesfully added' })
      } else {
        Toast.show({ message: response.message, mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Failed record creation operation', mode: 'danger', error: e })
    }
  }
  static #deleteRowClick = async (e, cell) => {
    Statuses.#currentOption = 'delete'
    console.log(cell.getRow().getData().dateTime)
    const formatoPersonalizado = "hh:mm a 'of' cccc d',' LLLL',' yyyy"
    let date = DateTime.fromISO(cell.getRow().getData().dateTime).toFormat(formatoPersonalizado, { locale: 'en' })
    const estado = Statuses.#listStatuses.find(item => item.key === cell.getRow().getData().deliveryStatus)

    Statuses.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: `<h5>Delete of status/h5>`,
      content: `<span class="text-back dark:text-gray-300">
                  Confirm the elimination of the status of the shipment:<br>
                  Id: ${cell.getRow().getData().id}<br>
                 Date and time: ${date}<br>
                  Status: ${estado.value}<br>
                </span>`,
      buttons: [
        { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Statuses.#delete(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Statuses.#modal.close() },
      ],
    })
    Statuses.#modal.show()
  }

  static async #delete(cell) {
    try {
      //Configurar la url para enviar la solicitud PATCH
      const url = `${urlAPI}/${Statuses.#shipmentType}/${Statuses.#guideNumber}/delete`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'PATCH',
      })

      if (response.message === 'ok') {
        Toast.show({ message: 'Status successfully deleted' })
        const response = await Helpers.fetchJSON(`${urlAPI}/${Statuses.#shipmentType}/id/${Statuses.#guideNumber}`)
        Statuses.#estadosTabla = response.data.statuses
        // Crear la tabla con los datos de la guía
        Statuses.createTable(response, Statuses.#guideNumber)
        Statuses.#modal.remove()
      } else {
        Toast.show({ message: response.message, mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems deleting the status', mode: 'danger', error: e })
    }
  }

  static #getFormData() {
    // recuerde utilizar parseInt(), parseFloat() o Number() cuando sea necesario
    return {
      statuses: [
        {
          deliveryStatus: document.querySelector(`#${Statuses.#modal.id} #status`).value,
          dateTime: document.querySelector(`#${Statuses.#modal.id} #dateTime`).value,
        },
      ],
    }
  }

  static #displayDataOnForm(idModal, rowData) {
    //por defecto, asignar a dateTime del formulario la fecha y hora actual
    const now = DateTime.now()
    document.querySelector('#form-statusesForm #dateTime').value = now.toFormat('yyyy-MM-dd HH:mm')
    const selectStatus = document.querySelector(`#${idModal} #status`)
    selectStatus.innerHTML = Statuses.#optStatuses
  }

  static #otherValidations() {
    const fechaNueva = new Date(document.querySelector(`#${Statuses.#modal.id} #dateTime`).value)

    const fechaMin = new Date(DateTime.now().minus({ hours: 1 }).toFormat('yyyy-MM-dd HH:mm'))

    const fechaMax = new Date(DateTime.now().plus({ hours: 1 }).toFormat('yyyy-MM-dd HH:mm'))

    if (fechaNueva < fechaMin || fechaNueva > fechaMax) {
      Toast.show({ message: 'The date and time must be less than the current date plus one hour and greater than the current date minus one hour.', mode: 'warning' })
      return false
    }

    return true
  }
}

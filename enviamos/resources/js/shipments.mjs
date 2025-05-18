export default class Shipments {
  static #table
  static #modal
  static #currentOption
  static #form
  static #mode
  static #customers

  constructor() {
    throw new Error('No requiere instancias, todos los métodos son estáticos. Use Shipments.init()')
  }

  static async init(mode = '') {
    Shipments.#mode = mode
    try {
      Shipments.#form = await Helpers.fetchText('./resources/html/shipments.html')
      let response = await Helpers.fetchJSON(`${urlAPI}/client`)
      if (response.message != 'ok') {
        throw new Error(`Error in response: ${JSON.stringify(response)}`) // JavaScript no cuenta con una clase Exception
      }
      // crear las opciones para un select de clientes
      Shipments.#customers = Helpers.toOptionList({
        items: response.data,
        value: 'id',
        text: 'name',
        firstOption: 'Select a client',
      })
      // intentar cargar los datos de los usuarios
      response = await Helpers.fetchJSON(`${urlAPI}/${mode}`)

      if (response.message != 'ok') {
        throw new Error(response.message)
      }

      //Obtener la lista de estados
      const response2 = await Helpers.fetchJSON(`${urlAPI}/delivery/statuses`)

      if (response2.message != 'ok') {
        throw new Error(response.message)
      }
      const listStatuses = response2.data

      // agregar al <main> de index.html la capa que contendrá la tabla
      document.querySelector('main').innerHTML = `
          <div class="p-2 w-full">
              <div id="table-container" class="m-2"></div>
          </dv>`

      Shipments.#table = new Tabulator('#table-container', {
        height: 'auto', // establecer la altura para habilitar el DOM virtual y mejorar la velocidad de procesamiento
        data: response.data,
        layout: 'fitDataTable', // ajustar columnas al ancho disponible. También fitData|fitDataFill|fitDataStretch|fitDataTable|fitColumns
        columns: [
          // definir las columnas de la tabla, para tipos datetime se utiliza formatDateTime definido en index.mjs
          { formatter: editRowButton, width: 40, hozAlign: 'center', cellClick: Shipments.#editRowClick },
          { formatter: deleteRowButton, width: 40, hozAlign: 'center', cellClick: Shipments.#deleteRowClick },
          { title: 'Number Guide', field: 'numGuide', hozAlign: 'center', width: 130 },
          { title: 'Content', field: 'content', width: 200 },
          { title: 'Sender', field: 'sender.name', width: 200 },
          { title: 'Addressee', field: 'addressee.name', hozAlign: 'center', width: 190 },
          { title: 'Value', field: 'value', hozAlign: 'center', width: 150, visible: mode !== 'envelope', formatter: 'money', formatterParams: { precision: 0 } },
          { title: 'Payment', field: 'payment', hozAlign: 'center', width: 120, formatter: 'money', formatterParams: { precision: 0 } },
          { title: 'Weight', field: 'weight', hozAlign: 'center', width: 90, visible: mode !== 'envelope' },
          {
            title: 'Fragile',
            field: 'isFragile',
            hozAlign: 'center',
            formatter: 'tickCross',
            width: 90,
            visible: mode !== 'envelope',
          },
          {
            title: 'Certified',
            field: 'isCertified',
            hozAlign: 'center',
            formatter: 'tickCross',
            width: 90,
            visible: mode === 'envelope',
          },

          {
            title: 'Status',
            field: 'statuses',
            formatter: function (cell) {
              const lista = cell.getValue() //Obtiene la lista que llega a la celda
              if (Array.isArray(lista) && lista.length > 0) {
                const lastStatus = lista[lista.length - 1] //Obtiene el ultimo elemento de la lista
                const dateStr = lastStatus.dateTime // las fechas al ser de tipo localDateTime vienen asi "2023-11-09T14:30:00"
                const date = DateTime.fromISO(dateStr)
                const status = listStatuses.find(current => current.key === lastStatus.deliveryStatus)

                // Formatea la fecha
                const formatedDate = date.toFormat('yyyy-MM-dd hh:mm')

                return `${status.value} ${formatedDate}`
              } else {
                return '' // Retorna vacío si la lista está vacía o no es un array
              }
            },
            hozAlign: 'center',
            width: 250,
          },
        ],
        responsiveLayout: false, // activado el scroll horizontal, también: ['hide'|true|false]
        // mostrar al final de la tabla un botón para agregar registros
        footerElement: `<div class='container-fluid d-flex justify-content-end p-0'>${addRowButton}</div>`,
      })
      Shipments.#table.on('tableBuilt', () => document.querySelector('#add-row').addEventListener('click', Shipments.#addRow))
    } catch (e) {
      Toast.show({ title: 'Shipments', message: 'Failure of the implementation load', mode: 'danger', error: e })
    }

    return this
  }

  static async #addRow() {
    Shipments.#currentOption = 'add'
    Shipments.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Entry of Shipments</h5>',
      content: Shipments.#form,
      buttons: [
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Shipments.#add() },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Shipments.#modal.close() },
      ],
      doSomething: Shipments.#displayDataOnForm,
    })
    Shipments.#modal.show()
  }

  static async #add() {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-shipments', Shipments.#otherValidations)) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud POST
      const body = Shipments.#getFormData()

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(`${urlAPI}/${Shipments.#mode}`, {
        method: 'POST',
        body,
      })

      if (response.message === 'ok') {
        Shipments.#table.addRow(response.data) // agregar la mercancía a la tabla
        Shipments.#modal.remove()
        Toast.show({ message: 'Successfully added' })
      } else {
        Toast.show({ message: response.message, mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Failed record creation operation', mode: 'danger', error: e })
    }
  }
  static #editRowClick = async (e, cell) => {
    Shipments.#currentOption = 'edit'
    Shipments.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Update of Shipments</h5>',
      content: Shipments.#form,
      buttons: [
        { caption: addButton, classes: 'btn btn-primary me-2', action: () => Shipments.#edit(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Shipments.#modal.close() },
      ],
      doSomething: idModal => Shipments.#displayDataOnForm(idModal, cell.getRow().getData()),
    })
    Shipments.#modal.show()
  }

  static async #edit(cell) {
    try {
      // verificar si los datos cumplen con las restricciones indicadas en el formulario HTML
      if (!Helpers.okForm('#form-shipments', Shipments.#otherValidations)) {
        return
      }
      // obtener del formulario el objeto con los datos que se envían a la solicitud PATCH
      const body = Shipments.#getFormData()

      //Configurar la url para enviar la solicitud PATCH
      const url = `${urlAPI}/${Shipments.#mode}/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'PATCH',
        body,
      })

      if (response.message === 'ok') {
        Toast.show({ message: `Shipment information successfully updated` })
        cell.getRow().update(response.data)
        Shipments.#modal.remove()
      } else {
        Toast.show({ message: response.message, mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems updating the shipment', mode: 'danger', error: e })
    }
  }

  static #deleteRowClick = async (e, cell) => {
    Shipments.#currentOption = 'delete'
    console.log(cell.getRow().getData())
    Shipments.#modal = new Modal({
      classes: 'col-12 col-sm-10 col-md-9 col-lg-8 col-xl-7',
      title: '<h5>Elimination of Shipments</h5>',
      content: `<span class="text-back dark:text-gray-300">Confirm the elimination of the box:<br>${cell.getRow().getData().id} - ${cell.getRow().getData().content}<br>Sender: ${cell.getRow().getData().sender.name}<br>Addressee: ${cell.getRow().getData().addressee.name}<br></span>`,
      buttons: [
        { caption: deleteButton, classes: 'btn btn-primary me-2', action: () => Shipments.#delete(cell) },
        { caption: cancelButton, classes: 'btn btn-secondary', action: () => Shipments.#modal.close() },
      ],
    })
    Shipments.#modal.show()
  }

  static async #delete(cell) {
    try {
      //Configurar la url para enviar la solicitud DELETE
      const url = `${urlAPI}/${Shipments.#mode}/${cell.getRow().getData().id}`

      // enviar la solicitud de creación con los datos del formulario
      let response = await Helpers.fetchJSON(url, {
        method: 'DELETE',
      })

      if (response.message === 'ok') {
        Toast.show({ message: `${Shipments.#mode} information successfully deleted` })
        cell.getRow().delete()
        Shipments.#modal.remove()
      } else {
        Toast.show({ message: 'Could not delete shipment', mode: 'danger', error: response })
      }
    } catch (e) {
      Toast.show({ message: 'Problems deleting the shipment', mode: 'danger', error: e })
    }
  }

  static async #displayDataOnForm(idModal, rowData) {
    // referenciar el select "cliente"
    const selectCustomers1 = document.querySelector(`#${idModal} #sender`)
    // asignar la lista de opciones al select "cliente" de Shipments.html
    selectCustomers1.innerHTML = Shipments.#customers

    const selectCustomers2 = document.querySelector(`#${idModal} #addressee`)

    selectCustomers2.innerHTML = Shipments.#customers

    // permita hacer visible el contenedor de la entrada que certifica o no el envío de un sobre y ocultar el contenedor de las entradas del peso y el valor. Así mismo, se podría aprovechar dicha condición para asignar por defecto 'Documentos' a el contenido y deshabilitar el checkbox de la entrada frágil
    if (Shipments.#mode === 'envelope') {
      document.querySelector(`#${idModal} #div-certified`).style.visibility = 'visible'
      document.querySelector(`#${idModal} #div-weight-value`).style.display = 'none'
      document.querySelector(`#${idModal} #content`).value = 'Documentos'
      document.querySelector(`#${idModal} #isFragile`).disabled = true
      document.querySelector(`#${idModal} #isFragile`).style.display = 'none'
      document.querySelector(`#${idModal} #div-fragile`).style.visibility = 'hidden'
    }

    //Para que cuando se vaya a actualizar los datos de un cliente en el formulario no aparezca vacio sino que aparezca ya los datos actuales
    if (Shipments.#currentOption === 'edit') {
      document.querySelector(`#${idModal} #content`).value = rowData.content
      document.querySelector(`#${idModal} #weight`).value = rowData.weight
      document.querySelector(`#${idModal} #value`).value = rowData.value
      document.querySelector(`#${idModal} #isFragile`).checked = rowData.isFragile
      selectCustomers1.value = rowData.sender.id
      selectCustomers2.value = rowData.addressee.id
      document.querySelector(`#${idModal} #isCertified`).checked = rowData.isCertified
    }
  }

  // Las operaciones de creación y actualización de Shipments necesitan de una función auxiliar que permita obtener del formulario de adición/actualización, los datos necesarios para crear un objeto que se pueda utilizar como body de la petición.
  // Como puede ver, esta función lo único que hace es asignar a cada atributo del objeto que se retorna, uno de los valores que hay en los campos de entrada del formulario.
  static #getFormData() {
    // recuerde utilizar parseInt(), parseFloat() o Number() cuando sea necesario
    return {
      content: document.querySelector(`#${Shipments.#modal.id} #content`).value,
      weight: parseFloat(document.querySelector(`#${Shipments.#modal.id} #weight`).value),
      isFragile: document.querySelector(`#${Shipments.#modal.id} #isFragile`).checked,
      value: parseFloat(document.querySelector(`#${Shipments.#modal.id} #value`).value),
      sender: document.querySelector(`#${Shipments.#modal.id} #sender`).value,
      addressee: document.querySelector(`#${Shipments.#modal.id} #addressee`).value,
      isCertified: document.querySelector(`#${Shipments.#modal.id} #isCertified`).checked,
    }
  }

  static #otherValidations() {
    //Referencie los elementos <select> sender y addressee
    const sender = document.querySelector(`#sender`)
    const addressee = document.querySelector(`#addressee`)

    if (sender.value === '') {
      Toast.show({ message: 'Sender selection is missing', mode: 'warning' })
      return false
    }

    if (addressee.value === '') {
      Toast.show({ message: 'Addressee selection is missing', mode: 'warning' })
      return false
    }

    if (sender.value === addressee.value) {
      Toast.show({ message: 'The addressee must be different from the sender.', mode: 'danger' })
      return false
    }

    if (document.querySelector(`#value`).value == 0 && Shipments.#mode != 'envelope') {
      Toast.show({ message: 'The estimated value cannot be zero', mode: 'warning' })
      return false
    }

    if (document.querySelector(`#weight`).value == 0 && Shipments.#mode != 'envelope') {
      Toast.show({ message: 'Weight cannot be zero', mode: 'warning' })
      return false
    }
    return true
  }

  static #toComplete(idModal, rowData) {
    console.warn('Sin implementar Shipments.toComplete()')
  }
}


export class BaseModal {
    constructor(callback, modalId) {
        this.callback = callback;
        this.modal = $(modalId ? modalId : "#add-modal");
        this.header = this.modal.find(".modal-title");
        this.body = this.modal.find(".modal-body");
        this.footer = this.modal.find(".modal-footer");
        this.footer.find(".btn-primary").off("click").on("click", () => {
            if (typeof callback === 'function') callback();
        });
    }

    startSpinner() {
        this.body.empty().append(`<i class="fa fa-spinner fa-spin 2x"></i>`);
    }

    stopSpinner() {
        this.body.find(`i.fa-spinner.fa-spin`).remove();
    }
}
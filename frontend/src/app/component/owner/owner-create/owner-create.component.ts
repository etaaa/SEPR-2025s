import {Component} from '@angular/core';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Owner} from 'src/app/dto/owner';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
import {OwnerService} from 'src/app/service/owner.service';

@Component({
  selector: 'app-owner-create',
  templateUrl: './owner-create.component.html',
  imports: [
    FormsModule
  ],
  standalone: true,
  styleUrls: ['./owner-create.component.scss']
})
export class OwnerCreateComponent {

  owner: Owner = {
    firstName: '',
    lastName: '',
    description: ''
  };

  constructor(
    private service: OwnerService,
    private router: Router,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    if (form.valid) {
      if (this.owner.description === '') {
        delete this.owner.description;
      }

      this.service.create(this.owner)
        .subscribe({
          next: data => {
            this.notification.success(`Owner ${this.owner.firstName} ${this.owner.lastName} successfully created.`);
            this.router.navigate(['/owners']);
          },
          error: error => {
            console.error('Error creating owner', error);
            this.notification.error(this.errorFormatter.format(error), 'Could Not Create Owner', {
              enableHtml: true,
              timeOut: 10000,
            });
          }
        });
    }
  }
}

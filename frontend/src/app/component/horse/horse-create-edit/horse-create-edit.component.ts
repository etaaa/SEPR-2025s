import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Horse, convertFromHorseToCreate} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {formatIsoDate} from "../../../utils/date-helper";
import {environment} from "../../../../environments/environment";

export enum HorseCreateEditMode {
  create,
  edit
}

const baseUri = environment.backendUrl;

@Component({
  selector: 'app-horse-create-edit',
  templateUrl: './horse-create-edit.component.html',
  imports: [
    CommonModule,
    FormsModule,
    AutocompleteComponent,
    FormsModule
  ],
  standalone: true,
  styleUrls: ['./horse-create-edit.component.scss']
})
export class HorseCreateEditComponent implements OnInit {

  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    //description: '',
    dateOfBirth: new Date(),
    sex: Sex.female,
  };
  horseBirthDateIsSet = false;
  selectedFile: File | null = null;
  imageSrc: string | null = null;
  deleteImageOnSubmit = false;


  constructor(
    private service: HorseService,
    private ownerService: OwnerService,
    private router: Router,
    private route: ActivatedRoute,
    private notification: ToastrService,
    private errorFormatter: ErrorFormatterService
  ) {
  }

  public get heading(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create New Horse';
      case HorseCreateEditMode.edit:
        return 'Edit Horse';
      default:
        return '?';
    }
  }

  public get submitButtonText(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'Create';
      case HorseCreateEditMode.edit:
        return 'Edit';
      default:
        return '?';
    }
  }

  public get horseBirthDateText(): string {
    if (!this.horseBirthDateIsSet) {
      return '';
    } else {
      return formatIsoDate(this.horse.dateOfBirth);
    }
  }

  public set horseBirthDateText(date: string) {
    if (date == null || date === '') {
      this.horseBirthDateIsSet = false;
    } else {
      this.horseBirthDateIsSet = true;
      this.horse.dateOfBirth = new Date(date);
    }
  }

  get modeIsCreate(): boolean {
    return this.mode === HorseCreateEditMode.create;
  }


  get sex(): string {
    switch (this.horse.sex) {
      case Sex.male:
        return 'Male';
      case Sex.female:
        return 'Female';
      default:
        return '';
    }
  }


  private get modeActionFinished(): string {
    switch (this.mode) {
      case HorseCreateEditMode.create:
        return 'created';
      case HorseCreateEditMode.edit:
        return 'edited';
      default:
        return '?';
    }
  }

  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  motherSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchByName(input, 5, 'FEMALE', this.horse.id!);

  fatherSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.searchByName(input, 5, 'MALE', this.horse.id!);

  ngOnInit(): void {
    this.route.data.subscribe(data => {
      this.mode = data.mode;
      if (this.mode === HorseCreateEditMode.edit) {
        const id = this.route.snapshot.paramMap.get('id');
        if (id) {
          this.service.getById(+id).subscribe({
            next: horse => {
              this.horse = horse;
              this.horseBirthDateIsSet = true;
              if (horse.imageUrl) {
                this.imageSrc = baseUri + horse.imageUrl;
              }
            },
            error: err => {
              console.error('Error loading horse', err);
            }
          });
        }
      }
    });
  }

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  public formatMotherName(mother: Horse | null | undefined): string {
    return (mother == null)
      ? ''
      : mother.name;
  }

  public formatFatherName(father: Horse | null | undefined): string {
    return (father == null)
      ? ''
      : father.name;
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.horse);
    if (form.valid) {
      if (this.horse.description === '') {
        delete this.horse.description;
      }

      const formData = new FormData();
      formData.append('name', this.horse.name);
      formData.append('dateOfBirth', this.horseBirthDateText);
      formData.append('sex', this.horse.sex);
      if (this.horse.description) {
        formData.append('description', this.horse.description);
      }
      if (this.horse.owner && this.horse.owner.id) {
        formData.append('ownerId', this.horse.owner.id.toString());
      }
      if (this.horse.mother && this.horse.mother.id) {
        formData.append('motherId', this.horse.mother.id.toString());
      }
      if (this.horse.father && this.horse.father.id) {
        formData.append('fatherId', this.horse.father.id.toString());
      }
      if (this.mode === HorseCreateEditMode.create) {
        if (this.selectedFile) {
          formData.append('image', this.selectedFile, this.selectedFile.name);
        }
      } else {
        if (this.selectedFile) {
          formData.append('image', this.selectedFile, this.selectedFile.name);
        }
        formData.append('deleteImage', this.deleteImageOnSubmit.toString());
      }

      let observable: Observable<Horse>;
      switch (this.mode) {
        case HorseCreateEditMode.create:
          observable = this.service.create(formData);
          break;
        case HorseCreateEditMode.edit:
          observable = this.service.update(this.horse.id!, formData);
          break;
        default:
          console.error('Unknown HorseCreateEditMode', this.mode);
          return;
      }
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          console.error('Error creating horse', error);
          this.notification.error(this.errorFormatter.format(error), 'Could Not Create Horse', {
            enableHtml: true,
            timeOut: 10000,
          });
        }
      });
    }
  }


  public onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      this.selectedFile = input.files[0];
      this.deleteImageOnSubmit = false;
      const reader = new FileReader();
      reader.onload = (e: any) => {
        this.imageSrc = e.target.result;
      };
      reader.readAsDataURL(this.selectedFile);
    }
  }

  public deleteImage(): void {
    this.selectedFile = null;
    this.imageSrc = null;
    if (this.mode === HorseCreateEditMode.edit) {
      this.deleteImageOnSubmit = true;
      this.horse.imageUrl = undefined;
    }
  }


}

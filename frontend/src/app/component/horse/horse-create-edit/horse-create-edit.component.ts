import {Component, OnInit} from '@angular/core';
import {CommonModule, Location} from '@angular/common';
import {FormsModule, NgForm, NgModel} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {Observable, of} from 'rxjs';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {Sex} from 'src/app/dto/sex';
import {ErrorFormatterService} from 'src/app/service/error-formatter.service';
import {HorseService} from 'src/app/service/horse.service';
import {OwnerService} from 'src/app/service/owner.service';
import {formatIsoDate} from "../../../utils/date-helper";
import {environment} from "../../../../environments/environment";

/**
 * Enum representing the mode of the horse create/edit component.
 */
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

/**
 * Component for creating or editing a horse's details.
 */
export class HorseCreateEditComponent implements OnInit {

  mode: HorseCreateEditMode = HorseCreateEditMode.create;
  horse: Horse = {
    name: '',
    description: '',
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
    private errorFormatter: ErrorFormatterService,
    private location: Location
  ) {
  }

  /**
   * Gets the heading text based on the current mode.
   *
   * @returns The heading string for the component
   */
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

  /**
   * Gets the submit button text based on the current mode.
   *
   * @returns The submit button text
   */
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

  /**
   * Gets the horse's birth date as an ISO-formatted string.
   *
   * @returns The formatted birth date or an empty string if not set
   */
  public get horseBirthDateText(): string {
    if (!this.horseBirthDateIsSet) {
      return '';
    } else {
      return formatIsoDate(this.horse.dateOfBirth);
    }
  }

  /**
   * Sets the horse's birth date from an ISO-formatted string.
   *
   * @param date The date string to set
   */
  public set horseBirthDateText(date: string) {
    if (date == null || date === '') {
      this.horseBirthDateIsSet = false;
    } else {
      this.horseBirthDateIsSet = true;
      this.horse.dateOfBirth = new Date(date);
    }
  }

  /**
   * Gets the display text for the horse's sex.
   *
   * @returns The sex as a string ('Male' or 'Female')
   */
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

  /**
   * Gets the action completion text based on the current mode.
   *
   * @returns The action text ('created' or 'edited')
   * @private
   */
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

  /**
   * Provides owner suggestions for the autocomplete feature.
   *
   * @param input The search string entered by the user
   * @returns An Observable of owner suggestions
   */
  ownerSuggestions = (input: string) => (input === '')
    ? of([])
    : this.ownerService.searchByName(input);

  /**
   * Provides mother suggestions for the autocomplete feature.
   *
   * @param input The search string entered by the user
   * @returns An Observable of female horse suggestions
   */
  motherSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.getAllOrSearch({name: input, limit: 5, sex: 'FEMALE', excludeId: this.horse.id!});

  /**
   * Provides father suggestions for the autocomplete feature.
   *
   * @param input The search string entered by the user
   * @returns An Observable of male horse suggestions
   */
  fatherSuggestions = (input: string) => (input === '')
    ? of([])
    : this.service.getAllOrSearch({name: input, limit: 5, sex: 'MALE', excludeId: this.horse.id!});

  /**
   * Cancels the create/edit process and navigates back to the previous page.
   */
  public onCancel(): void {
    this.location.back();
  }

  /**
   * Initializes the component by setting the mode and loading horse data if in edit mode.
   */
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
              this.notification.error(this.errorFormatter.format(err), 'Could Not Load Horse', {
                enableHtml: true,
                timeOut: 10000,
              });
              this.router.navigate(['/horses']);
            }
          });
        } else {
          this.router.navigate(['/horses']);
        }
      }
    });
  }

  /**
   * Determines dynamic CSS classes for form inputs based on their validation state.
   *
   * @param input The form input model to evaluate
   * @returns An object containing CSS classes (e.g., 'is-invalid')
   */
  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  /**
   * Formats an owner's name for display.
   *
   * @param owner The owner object to format
   * @returns The formatted owner name or an empty string if null/undefined
   */
  public formatOwnerName(owner: Owner | null | undefined): string {
    return (owner == null)
      ? ''
      : `${owner.firstName} ${owner.lastName}`;
  }

  /**
   * Formats a mother's name for display.
   *
   * @param mother The mother horse object to format
   * @returns The mother's name or an empty string if null/undefined
   */
  public formatMotherName(mother: Horse | null | undefined): string {
    return (mother == null)
      ? ''
      : mother.name;
  }

  /**
   * Formats a father's name for display.
   *
   * @param father The father horse object to format
   * @returns The father's name or an empty string if null/undefined
   */
  public formatFatherName(father: Horse | null | undefined): string {
    return (father == null)
      ? ''
      : father.name;
  }

  /**
   * Submits the horse create/edit form and handles the creation or update process.
   *
   * @param form The form containing the horse data
   */
  public onSubmit(form: NgForm): void {
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
          return;
      }
      observable.subscribe({
        next: data => {
          this.notification.success(`Horse ${this.horse.name} successfully ${this.modeActionFinished}.`);
          this.router.navigate(['/horses']);
        },
        error: error => {
          this.notification.error(this.errorFormatter.format(error), `Could Not ${this.submitButtonText} Horse`, {
            enableHtml: true,
            timeOut: 10000,
          });
        }
      });
    }
  }

  /**
   * Handles the selection of an image file for the horse.
   *
   * @param event The file input event
   */
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

  /**
   * Deletes the selected or existing image for the horse.
   */
  public deleteImage(): void {
    this.selectedFile = null;
    this.imageSrc = null;
    if (this.mode === HorseCreateEditMode.edit) {
      this.deleteImageOnSubmit = true;
      this.horse.imageUrl = undefined;
    }
  }
}

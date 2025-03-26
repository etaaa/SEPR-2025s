import {Component, OnInit} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {RouterLink} from '@angular/router';
import {ToastrService} from 'ngx-toastr';
import {AutocompleteComponent} from 'src/app/component/autocomplete/autocomplete.component';
import {HorseService} from 'src/app/service/horse.service';
import {Horse} from 'src/app/dto/horse';
import {Owner} from 'src/app/dto/owner';
import {ConfirmDeleteDialogComponent} from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
import {OwnerService} from "../../service/owner.service";
import {Observable, of} from "rxjs";

@Component({
  selector: 'app-horse',
  templateUrl: './horse.component.html',
  imports: [
    RouterLink,
    FormsModule,
    AutocompleteComponent,
    ConfirmDeleteDialogComponent
  ],
  standalone: true,
  styleUrls: ['./horse.component.scss']
})

/**
 * Component for displaying and managing a list of horses with search and deletion capabilities.
 */
export class HorseComponent implements OnInit {
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseForDeletion: Horse | undefined;
  searchFilter: {
    name?: string,
    description?: string,
    dateOfBirth?: string,
    sex?: string,
    owner?: string
  } = {
    name: '',
    description: '',
    dateOfBirth: '',
    sex: '',
    owner: ''
  };

  constructor(
    private service: HorseService,
    private notification: ToastrService,
    private ownerService: OwnerService,
  ) {
  }

  /**
   * Initializes the component by loading the list of horses.
   */
  ngOnInit(): void {
    this.reloadHorses();
  }

  /**
   * Provides owner suggestions for the autocomplete feature based on input.
   *
   * @param input The search string entered by the user
   * @returns An Observable of owner suggestions
   */
  ownerSuggestions = (input: string): Observable<Owner[]> => (input === '')
    ? of([])
    : this.ownerService.searchByName(input);

  /**
   * Formats an owner object or string into a display-friendly name.
   *
   * @param owner The owner object or string to format
   * @returns The formatted owner name or an empty string if null/undefined
   */
  formatOwnerName(owner: Owner | string | null | undefined): string {
    if (!owner) {
      return '';
    }
    if (typeof owner === 'string') {
      return owner;
    }
    return `${owner.firstName} ${owner.lastName}`;
  }

  /**
   * Reloads the list of horses based on current search filters.
   */
  reloadHorses() {
    console.log('searchFilter:', this.searchFilter);
    this.service.getAllOrSearch(this.searchFilter)
      .subscribe({
        next: data => {
          this.horses = data;
          this.bannerError = null;
        },
        error: error => {
          console.error('Error fetching horses', error);
          this.bannerError = 'Could not fetch horses: ' + error.message;
          const errorMessage = error.status === 0
            ? 'Is the backend up?'
            : error.message.message;
          this.notification.error(errorMessage, 'Could Not Fetch Horses');
        }
      });
  }

  /**
   * Handles the search form submission by reloading horses with current filters.
   *
   * @param event The form submission event
   */
  onSearchSubmit(event: Event) {
    event.preventDefault();
    this.reloadHorses();
  }

  /**
   * Formats an owner's full name for display.
   *
   * @param owner The owner object to format
   * @returns The owner's full name or an empty string if null
   */
  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  /**
   * Converts a horse's date of birth to a localized date string.
   *
   * @param horse The horse object containing the date of birth
   * @returns The formatted date string
   */
  dateOfBirthAsLocaleDate(horse: Horse): string {
    return horse.dateOfBirth.toLocaleDateString();
  }

  /**
   * Deletes a specified horse and refreshes the horse list.
   *
   * @param horse The horse to delete
   */
  deleteHorse(horse: Horse) {
    this.service.deleteHorse(horse).subscribe({
      next: () => {
        this.reloadHorses();
      }
      // Error handling is already done in the service
    });
  }
}

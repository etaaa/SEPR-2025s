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
export class HorseComponent implements OnInit {
  horses: Horse[] = [];
  bannerError: string | null = null;
  horseForDeletion: Horse | undefined;
  searchFilter = {
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

  ngOnInit(): void {
    this.reloadHorses();
  }

  ownerSuggestions = (input: string): Observable<Owner[]> => (input === '')
    ? of([])
    : this.ownerService.searchByName(input, 5);

  formatOwnerName(owner: Owner | string | null | undefined): string {
    if (!owner) {
      return '';
    }
    if (typeof owner === 'string') {
      return owner;
    }
    return `${owner.firstName} ${owner.lastName}`;
  }

  reloadHorses() {
    this.service.getAll(this.searchFilter)
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

  onSearchSubmit(event: Event) {
    event.preventDefault();
    this.reloadHorses();
  }

  ownerName(owner: Owner | null): string {
    return owner
      ? `${owner.firstName} ${owner.lastName}`
      : '';
  }

  dateOfBirthAsLocaleDate(horse: Horse): string {
    return horse.dateOfBirth.toLocaleDateString();
  }

  deleteHorse(horse: Horse) {
    this.service.deleteHorse(horse).subscribe({
      next: () => {
        this.reloadHorses();
      }
      // Error handling is already done in the service
    });
  }
}

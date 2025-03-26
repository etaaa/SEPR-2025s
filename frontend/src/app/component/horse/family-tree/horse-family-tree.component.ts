import {Component, OnInit, OnDestroy} from '@angular/core';
import {CommonModule, Location} from '@angular/common';
import {ActivatedRoute, Router, RouterLink} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {HttpClient, HttpParams} from '@angular/common/http';
import {ToastrService} from 'ngx-toastr';
import {environment} from 'src/environments/environment';
import {Horse} from 'src/app/dto/horse';
import {HorseService} from 'src/app/service/horse.service';
import {ConfirmDeleteDialogComponent} from 'src/app/component/confirm-delete-dialog/confirm-delete-dialog.component';
import {Subscription} from 'rxjs';
import {ErrorFormatterService} from "../../../service/error-formatter.service";

/**
 * Interface representing a horse in the family tree with additional properties.
 */
interface FamilyTreeHorse extends Horse {
  /**
   * Indicates whether the horse's family tree is expanded in the UI.
   */
  isExpanded?: boolean;

  /**
   * The horse's mother in the family tree.
   */
  mother?: FamilyTreeHorse;

  /**
   * The horse's father in the family tree.
   */
  father?: FamilyTreeHorse;
}

@Component({
  selector: 'app-horse-family-tree',
  templateUrl: './horse-family-tree.component.html',
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    ConfirmDeleteDialogComponent
  ],
  standalone: true,
  styleUrls: ['./horse-family-tree.component.scss']
})

/**
 * Component for displaying and managing a horse's family tree.
 */
export class HorseFamilyTreeComponent implements OnInit, OnDestroy {
  horse: FamilyTreeHorse | null = null;
  loading = true;
  error = false;
  generations = 3;
  horseForDeletion: Horse | undefined;
  private routeSubscription: Subscription | null = null;
  private baseUri = environment.backendUrl;

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router,
    private notification: ToastrService,
    private service: HorseService,
    private errorFormatter: ErrorFormatterService,
    private location: Location
  ) {
  }

  /**
   * Initializes the component by subscribing to route parameters and loading the family tree.
   */
  ngOnInit(): void {
    this.routeSubscription = this.route.paramMap.subscribe(params => {
      const id = params.get('id');
      if (id) {
        this.route.queryParamMap.subscribe(queryParams => {
          const gen = queryParams.get('generations');
          if (gen) {
            this.generations = +gen;
          }
          this.loadFamilyTree(+id, this.generations);
        });
      } else {
        this.error = true;
        this.loading = false;
      }
    });
  }

  /**
   * Cleans up the component by unsubscribing from route parameter subscription.
   */
  ngOnDestroy(): void {
    if (this.routeSubscription) {
      this.routeSubscription.unsubscribe();
    }
  }

  /**
   * Loads the family tree for a horse with the specified ID and number of generations.
   *
   * @param id The ID of the horse to load the family tree for
   * @param generations The number of generations to include in the family tree
   */
  loadFamilyTree(id: number, generations: number): void {
    this.loading = true;
    this.error = false;

    let params = new HttpParams()
      .set('generations', generations.toString());

    this.http.get<FamilyTreeHorse>(`${this.baseUri}/horses/${id}/familytree`, {params})
      .subscribe({
        next: (data) => {
          this.horse = this.processHorseData(data);
          this.loading = false;
        },
        error: error => {
          console.error('Error loading family tree', error);
          this.notification.error(this.errorFormatter.format(error), 'Could not load family tree', {
            enableHtml: true,
            timeOut: 10000,
          });
          this.loading = false;
          this.error = true;
        }
      });
  }

  /**
   * Processes horse data recursively to fix dates and set expansion state.
   *
   * @param horse The horse object to process
   * @returns The processed horse object with updated properties
   * @private
   */
  private processHorseData(horse: FamilyTreeHorse): FamilyTreeHorse {
    if (!horse) return horse;
    horse.dateOfBirth = new Date(horse.dateOfBirth as unknown as string);
    horse.isExpanded = true;
    if (horse.mother) {
      horse.mother = this.processHorseData(horse.mother);
    }
    if (horse.father) {
      horse.father = this.processHorseData(horse.father);
    }

    return horse;
  }

  /**
   * Toggles the expanded state of a horse in the family tree.
   *
   * @param horse The horse whose expansion state should be toggled
   */
  toggleExpand(horse: FamilyTreeHorse): void {
    horse.isExpanded = !horse.isExpanded;
  }

  /**
   * Updates the number of generations displayed in the family tree and reloads it.
   */
  updateGenerations(): void {
    if (this.generations > 10) {
      this.notification.error('Maximum generations allowed is 10', 'Invalid Input');
      return;
    }
    if (this.generations < 1) {
      this.notification.error('Minimum generations allowed is 1', 'Invalid Input');
      return;
    }

    if (this.horse && this.horse.id) {
      this.router.navigate([], {
        relativeTo: this.route,
        queryParams: {generations: this.generations},
        queryParamsHandling: 'merge'
      }).then(() => {
        if (this.horse && this.horse.id !== undefined) {
          this.loadFamilyTree(this.horse.id, this.generations);
        } else {
          console.error('Cannot load family tree: Horse ID is missing');
          this.notification.error('Cannot load family tree', 'Missing horse ID');
        }
      });
    }
  }

  /**
   * Navigates back to the previous page in the browser history.
   */
  public goBack(): void {
    this.location.back();
  }

  /**
   * Validates and corrects the generations input field value.
   *
   * @param event The input event from the generations field
   */
  validateGenerations(event: Event): void {
    const input = event.target as HTMLInputElement;
    const value = parseInt(input.value, 10);
    if (value > 10) {
      input.value = '10';
      this.generations = 10;
      this.notification.error('Maximum generations allowed is 10', 'Invalid Input');
    } else if (value < 1) {
      input.value = '1';
      this.generations = 1;
      this.notification.error('Minimum generations allowed is 1', 'Invalid Input');
    }
  }

  /**
   * Formats a date object as a localized date string.
   *
   * @param date The date to format
   * @returns The formatted date string
   */
  formatDate(date: Date): string {
    return date.toLocaleDateString();
  }

  /**
   * Deletes a horse and updates the family tree or navigates away if the root horse is deleted.
   *
   * @param horse The horse to delete
   */
  deleteHorse(horse: Horse): void {
    this.service.deleteHorse(horse).subscribe({
      next: () => {
        if (horse.id === this.horse?.id) {
          this.router.navigate(['/horses']);
        } else {
          if (this.horse && this.horse.id !== undefined) {
            this.loadFamilyTree(this.horse.id, this.generations);
          } else {
            console.error('Cannot load family tree: Horse ID is missing');
            this.notification.error('Cannot load family tree', 'Missing horse ID');
          }
        }
      },
      error: () => {
        // Error handling is already done in the service
      }
    });
  }
}

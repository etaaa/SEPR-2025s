import {Routes} from '@angular/router';
import {
  HorseCreateEditComponent,
  HorseCreateEditMode
} from './component/horse/horse-create-edit/horse-create-edit.component';
import {HorseComponent} from './component/horse/horse.component';
import {HorseDetailComponent} from "./component/horse/horse-detail/horse-detail.component";
import {OwnerComponent} from "./component/owner/owner.component";
import {OwnerCreateComponent} from "./component/owner/owner-create/owner-create.component";
import {HorseFamilyTreeComponent} from "./component/horse/family-tree/horse-family-tree.component";


export const routes: Routes = [
  {
    path: 'horses', children: [
      {path: '', component: HorseComponent},
      {path: 'create', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.create}},
      {path: ':id/detail', component: HorseDetailComponent},
      {path: ':id/familytree', component: HorseFamilyTreeComponent},
      {path: ':id/edit', component: HorseCreateEditComponent, data: {mode: HorseCreateEditMode.edit}}
    ]
  },
  {
    path: 'owners', children: [
      {path: '', component: OwnerComponent},
      {path: 'create', component: OwnerCreateComponent}
    ]
  },
  {path: '**', redirectTo: 'horses'},
];

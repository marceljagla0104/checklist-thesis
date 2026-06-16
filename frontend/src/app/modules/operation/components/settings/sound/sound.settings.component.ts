import { Component, OnInit } from "@angular/core";
import {TranslateService} from '@ngx-translate/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {take} from 'rxjs';

@Component({
    selector: 'app-settings-sound',
    templateUrl: './sound.settings.component.html',
    styleUrls: ['./sound.settings.component.scss']   
})
export class SoundSettingsComponent implements OnInit{
    errorSoundEnabled: boolean = true;

    constructor(
        private translate: TranslateService,
        private snackBar: MatSnackBar,
    ) {}

    toggleErrorSound(){
        localStorage.setItem('errorSoundEnabled', this.errorSoundEnabled.toString());
    }

    private showSuccessMessage(){
        this.translate
            .get('SETTINGS_AUDIO_SUCCESS')
            .pipe(take(1))
            .subscribe((res: string) => {
                this.snackBar.open(res, null, {
                    duration: 2000,
                    panelClass: 'green-snackbar'
                });
            });
    }

    ngOnInit(): void {
        const savedSoundSetting = localStorage.getItem('errorSoundEnabled');  //Wenn noch nie benutzt und lokaler speicher leer ist... Standardmaessig auf true
        this.errorSoundEnabled = savedSoundSetting ? savedSoundSetting === 'true' :true;
    }

}
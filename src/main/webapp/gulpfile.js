var gulp = require('gulp'),
    sass = require('gulp-sass'),
    filter = require('gulp-filter'),
    autoprefixer = require('gulp-autoprefixer'),
    concat = require('gulp-concat'),
    uglify = require('gulp-uglify'),
    imagemin = require('gulp-imagemin'),
    pump = require('pump');

var config = {
    stylesPath: 'styles/sass',
    jsPath: 'styles/js',
    localLibsPath: 'styles/libs',
    imagesPath: 'images',
    outputDir: 'assets'
};

gulp.task('font-awesome-css', function (cb) {
    pump([
        gulp.src(config.localLibsPath + '/fa/*'),
        filter('**/*.css'),
        concat("fontawesome.css"),
        gulp.dest(config.outputDir + '/css')
    ], cb);
});

gulp.task('font-awesome-js', function (cb) {
    pump([
        gulp.src(config.localLibsPath + '/fa/*'),
        filter('**/*.js'),
        uglify(),
        concat('fontawesome.js'),
        gulp.dest(config.outputDir + '/js')
    ], cb);
});


gulp.task('jquery', function (cb) {
    pump([
        gulp.src('./node_modules/jquery/dist/*'),
        filter('**/jquery.min.*'),
        gulp.dest(config.outputDir + '/js')
    ], cb);
});

gulp.task('sweet-alerts', function () {
    return gulp.src('./node_modules/sweetalert/dist/sweetalert.min.js')
        .pipe(gulp.dest(config.outputDir + '/js'));
});


gulp.task('lightbox', function () {
    return gulp.src('./node_modules/lightbox2/dist/js/lightbox.min.*')
            .pipe(gulp.dest(config.outputDir + '/js')) &&
        gulp.src('./node_modules/lightbox2/dist/images/**/*')
            .pipe(gulp.dest(config.outputDir + '/images')) &&
        gulp.src('./node_modules/lightbox2/dist/css/lightbox.min.css')
            .pipe(gulp.dest(config.outputDir + '/css'));
});

gulp.task('libs', gulp.parallel('jquery', 'font-awesome-css', 'font-awesome-js', 'sweet-alerts', 'lightbox'));


gulp.task('images', function () {
    return gulp.src(config.imagesPath + '/*')
        .pipe(imagemin())
        .pipe(gulp.dest(config.outputDir + '/images'))
});

gulp.task('css', function (cb) {
    pump([
            gulp.src(config.stylesPath + '/**/*.scss'),
            sass({
                outputStyle: 'compressed',
                includePaths: [
                    config.stylesPath,
                    './node_modules/font-awesome/scss'
                ]
            }).on('error', sass.logError),
            autoprefixer(),
            gulp.dest(config.outputDir + '/css')
        ], cb);
});

gulp.task('js', function (cb) {
    pump([
            gulp.src(config.jsPath + '/*'),
            filter('**/*.js'),
            uglify(),
            gulp.dest(config.outputDir + '/js')
        ],
        cb
    );
});

gulp.task('js-ie', function (cb) {
    pump([
            gulp.src(config.jsPath + '/ie/*'),
            filter('**/*.js'),
            uglify(),
            gulp.dest(config.outputDir + '/js/ie')
        ],
        cb
    );
});

gulp.task('watch', function () {
    gulp.watch([config.stylesPath + '**/*.scss', config.stylesPath + '**/*.sass', config.stylesPath + '**/*.css'], ['css']);
    gulp.watch([config.jsPath + '**/*.js'], ['js']);
    gulp.watch([config.imagesPath + '/**/*'], ['images']);
});

gulp.task('build', gulp.series('libs', 'images', 'css', 'js-ie', 'js'));

gulp.task('default', gulp.parallel('build'));

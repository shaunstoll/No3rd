const { filter, omit, prop, compose, flatten, concat,
    equals, reject, join, map, match, trim, reduce, max, replace,
    head, nth, split, contains, values, curry, isNil, set, lensProp } = require('ramda')
const fs = require('fs')
const cheerio = require('cheerio');
const axios = require('axios')
const Nightmare = require('nightmare')
const nightmare = Nightmare({
    show: true,
    // openDevTools: {
    //     mode: 'detach'
    // }
})

// Custom functions
var $;
const loadHtml = (html) => $ = cheerio.load(html)
const el2str = el => $(el).html() //Convert html nodes to strings
const flapit = (char) => compose(flatten, map(split(char))) // To further split an array
const filterFor = (str) => filter(contains(str)) // Filters array for elements containing string
const longestSubArr = reduce(max, []) // Gives longest subarray

const isWeightStr = /[0-9]+\.*[0-9]*\s(pounds|ounces)/g; // Matches 1.24 pounds or 2 ounces etc...
const isDimensions = /[0-9]+\.*[0-9]*\sx\s[0-9]+\.*[0-9]*\sx\s[0-9]+\.*[0-9]*\s(inches|feet)/g; // matches 11 x 11 x 2.9 inches

const scrape = searchPhrase => {
    let url = `https://www.amazon.com/s/ref=nb_sb_noss?url=search-alias%3Daps&field-keywords=${searchPhrase}`
    return nightmare
        .goto(url)
        .wait('#resultsCol')
        .click('#resultsCol > #centerMinus a.s-access-detail-page')
        .wait('body')
        .evaluate(() => {
            let elems = document.querySelectorAll('.imageThumbnail, .videoThumbnail');
            elems.forEach(function (elem) {
                elem.click();
            });
            return "sucsess";
        })
        .wait(2000)
        .evaluate(() => document.body.innerHTML)
        .then(html => {
            //fs.writeFileSync(`${searchPhrase}.body.html`,html)
            //html = fs.readFileSync(`${searchPhrase}.body.html`,{encoding:'utf-8'})
            loadHtml(html)

            // Get product details
            const arr1 = $('#productDetails_detailBullets_sections1 > tbody > tr').toArray()
            const arr2 = $('#detail-bullets div.content ul li').toArray()
            const arr = longestSubArr([arr1, arr2])

            // Get dimensions
            try {
                var dimensions = compose(reject(equals('x')), split(" "), trim, head, match(isDimensions), head, filterFor('Product Dimensions'), map(el2str))(arr);
                var item_length = compose(Number, head)(dimensions)
                var item_width = compose(Number, nth(1))(dimensions)
                var item_height = compose(Number, nth(2))(dimensions)
                var dimension_unit = nth(3)(dimensions)

                // Get weight
                weightValueAndUnit = compose(flapit(' '), match(isWeightStr), head, filterFor('Shipping Weight'), map(el2str))(arr)
                var wtVal = compose(Number, head)(weightValueAndUnit)
                var wtUnit = nth(1)(weightValueAndUnit)
            } catch (e) {
                console.log("caught", e)
            }

            //Get title
            const title = $('#productTitle').text().trim()

            // Get images
            const urls = $('#main-image-container > ul > li.item img').map((i, el) => $(el).prop('src')).toArray().join(',')

            // Get Video
            const vidUrl = $('#main-video-container video').prop('src')

            // Get description
            const description = $('#descriptionAndDetails').html()

            // manufacturer
            let manufacturer = ""
            let price = ""
            try {
                manufacturer = $('#bylineInfo').html()
                price = $('#priceblock_ourprice').html()
            } catch (e) {
                console.log("could not get manufacturer string")
            }

            const res = { searched: searchPhrase, wtVal, wtUnit, title, urls, item_length, item_width, item_height, dimension_unit, vidUrl, manufacturer, description, price }

            return res

        }).catch(console.error)

}




const tryfn = (fn) => {
    try {
        return fn()
    } catch (e) {
        console.error(e)
    }
}
const getTitle = ($) => tryfn(() => $('#productTitle').html().trim())
const getPrice = ($) => tryfn(() => $('#priceblock_ourprice').html().trim())
const getImages = ($) => tryfn(() => {
    return eval('(' + $('script')
        .filter((i, el) => $(el).html().indexOf('ImageBlockATF') > -1)
        .map((i, el) => $(el).html())[0]
        .split(/(var data =)([\s\S]*?)(;\s*A.trigger)/g)[2] + ')')
        .colorImages.initial.map(el => el.hiRes)
        .filter(el => !isNil(el))
        .join(', ')
})
const getSoldBy = ($) => tryfn(() => $('#bylineInfo').html())
const getAvailability = ($) => tryfn(() => $('#availability > span').html().trim())
const getAvailabilityColor = ($) => tryfn(() => $('#availability > span').attr('class').split(/\s+/).join(', '))
const getDescription = ($) => tryfn(() => $('#descriptionAndDetails').html())
const getDetails = ($) => tryfn(() => {
    const v1 = $('#productDetails_detailBullets_sections1 > tbody > tr')
        .map(
            (i, el) => { 
                const header = $(el).children('th').text().trim()
                const value = $(el).children('td').text().trim()
                return { header, value }

            }
        )
    const v2 = $('.pdTab tbody > tr')
        .map(
            (i, el) => {
                const header = $(el).children('td').eq(0).html()
                const value = $(el).children('td').eq(1).html()
                return { header, value }
            })
    //const v3 detail_bullets_id
    return v1.length == 0 ? v2 : v1
})
const getTechnicalDetails = ($) => tryfn(() => {
    return $('#productDetails_techSpec_section_1 > tbody > tr')
        .map(
            (i, el) => {
                const header = $(el).children('th').text().trim()
                const value = $(el).children('td').text().trim()
                return { header, value }

            }
        )
})
const getAsin = ($) => tryfn(() => compose(prop('value'), head)(getDetails($).toArray().filter(el => el.header == 'ASIN')))
const getManufacturer = ($) => tryfn(() => compose(prop('value'), head)(getDetails($).toArray().filter(el => el.header == 'Manufacturer')))
const getShippingWeight = ($) => tryfn(() => compose(prop('value'), head)(getDetails($).toArray().filter(el => el.header == 'Shipping Weight')))

const getItemWeight = ($) => tryfn(() => compose(prop('value'), head)(getTechnicalDetails($).toArray().filter(el => el.header == 'Item Weight')))
const getProductDimensions = ($) => tryfn(() => compose(prop('value'), head)(getTechnicalDetails($).toArray().filter(el => el.header == 'Product Dimensions')))
const getItemModelNumber = ($) => tryfn(() => compose(prop('value'), head)(getTechnicalDetails($).toArray().filter(el => el.header == 'Item model number')))

const scrapeAsins = (asins, market) => {
    console.log(asins)
    const scrape = curry((market, asin) => {
        const url = (market == 'us') ? `http://www.amazon.com/dp/${asin}` : `http://www.amazon.ca/dp/${asin}`
        return nightmare.get(url)
            .then(res => {
                const html = res.data
                const $ = cheerio.load(html)

                const x = getDetails($)
                //const getImage = () => tryfn(() => $('#imgTagWrapperId img').attr('data-old-hires'))
                //$('script').filter((i,el)=>$(el).html().indexOf('ImageBlockATF') > -1).map((i,el)=>$(el).html())[0].split(/(var data =)([\s\S]*?)(A.trigger)/g)[2]

                const parsed = {
                    title: getTitle($),
                    price: getPrice($),
                    // image_single: getImage($),
                    images: getImages($),
                    sold_by: getSoldBy($),
                    availability: getAvailability($),
                    availabilityColor: getAvailabilityColor($),
                    asin: getAsin($),
                    manufacturer: getManufacturer($),
                    shipping_weight: getShippingWeight($),
                    item_weight: getItemWeight($),
                    dimensions: getProductDimensions($),
                    item_model_number: getItemModelNumber($),
                    description: getDescription($),
                }

                //console.log(set(lensProp('description'), parsed.description.substring(0, 5) + '...', parsed))
                console.log(omit(parsed))
                return parsed
            })
            .catch(console.error)
    })

    return Promise.all(map(scrape(market))(asins))
}



const asyncLoop = async (searchPhrases, asinMode, market) => {
    if (asinMode) {
        return scrapeAsins(searchPhrases, market)
    }
    //console.log(searchPhrases.length)
    const headers = 'i,searched,wtVal,wtUnit, title, urls, item_length, item_width, item_height, dimension_unit, vidUrl, manufacturer, description, price';
    //console.log(headers)
    fs.writeFileSync('output.csv', headers)

    let output = [];
    //output += headers // Added headers row to this functions output response

    for (let i = 0; i < searchPhrases.length; i++) {
        const result = await scrape(searchPhrases[i]);
        console.log({ i, ...omit(['urls', 'description'])(result) })
        const escapeCSVCell = el => typeof el == 'string' ? `"${replace(/"/g, '""', el)}"` : el
        let output_line = compose(concat('\n'), join(','), map(escapeCSVCell), concat([i + 1]), values)(result)

        fs.appendFileSync('output.csv', output_line)
        output.push({ i, ...result })
    }

    return output

}

exports.scrapePhrases = asyncLoop

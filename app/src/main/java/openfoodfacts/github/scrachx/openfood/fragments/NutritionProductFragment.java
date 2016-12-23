package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevelItem;
import openfoodfacts.github.scrachx.openfood.models.NutrientLevels;
import openfoodfacts.github.scrachx.openfood.models.Nutriments;
import openfoodfacts.github.scrachx.openfood.models.Product;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.adapters.NutrientLevelListAdapter;
import openfoodfacts.github.scrachx.openfood.views.customtabs.CustomTabActivityHelper;
import openfoodfacts.github.scrachx.openfood.views.customtabs.WebViewFallback;

import static openfoodfacts.github.scrachx.openfood.utils.Utils.bold;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class NutritionProductFragment extends BaseFragment implements CustomTabActivityHelper.ConnectionCallback {

    @BindView(R.id.imageGrade) ImageView img;
    @BindView(R.id.listNutrientLevels) ListView lv;
    @BindView(R.id.textServingSize) TextView serving;
    @BindView(R.id.textCarbonFootprint) TextView carbonFootprint;
    private CustomTabActivityHelper customTabActivityHelper;
    private Uri nutritionScoreUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return createView(inflater, container, R.layout.fragment_nutrition_product);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Intent intent = getActivity().getIntent();
        State state = (State) intent.getExtras().getSerializable("state");

        final Product product = state.getProduct();
        NutrientLevels nt = product.getNutrientLevels();
        ArrayList<NutrientLevelItem> levelItem = new ArrayList<>();

        if (nt == null || (nt.getFat() == null && nt.getSalt() == null
                && nt.getSaturatedFat() == null && nt.getSugars() == null)) {
            levelItem.add(new NutrientLevelItem(getString(R.string.txtNoData), R.drawable.error_image));
        } else {
            // prefetch the uri
            customTabActivityHelper = new CustomTabActivityHelper();
            customTabActivityHelper.setConnectionCallback(this);
            // currently only available in french translations
            nutritionScoreUri = Uri.parse("http://fr.openfoodfacts.org/score-nutritionnel-france");
            customTabActivityHelper.mayLaunchUrl(nutritionScoreUri, null, null);

            String fatTxt = Html.fromHtml("<b>" + getString(R.string.txtFat) + "</b>" + ' ' + localiseNutritionLevel(nt.getFat()) + " (" + product.getNutriments().getFat100g() + product.getNutriments().getFatUnit() + ")").toString();
            String saturatedFatTxt = Html.fromHtml("<b>" + getString(R.string.txtSaturatedFat) + "</b>" + ' ' + localiseNutritionLevel(nt.getSaturatedFat()) + " (" + product.getNutriments().getSaturatedFat100g() + product.getNutriments().getSaturatedFatUnit() + ")").toString();
            String sugarsTxt = Html.fromHtml("<b>" + getString(R.string.txtSugars) + "</b>" + ' ' + localiseNutritionLevel(nt.getSugars()) + " (" + product.getNutriments().getSugars100g() + product.getNutriments().getSugarsUnit() + ")").toString();
            String saltTxt = Html.fromHtml("<b>" + getString(R.string.txtSalt) + "</b>" + ' ' + localiseNutritionLevel(nt.getSalt()) + " (" + product.getNutriments().getSalt100g() + product.getNutriments().getSaltUnit() + ")").toString();

            levelItem.add(new NutrientLevelItem(fatTxt, getImageLevel(nt.getFat())));
            levelItem.add(new NutrientLevelItem(saturatedFatTxt, getImageLevel(nt.getSaturatedFat())));
            levelItem.add(new NutrientLevelItem(sugarsTxt, getImageLevel(nt.getSugars())));
            levelItem.add(new NutrientLevelItem(saltTxt, getImageLevel(nt.getSalt())));

            img.setImageResource(getImageGrade(product.getNutritionGradeFr()));
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                Bitmap icon = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_navigation_arrow_back)).getBitmap();

                CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder(customTabActivityHelper.getSession())
                        .setShowTitle(true)
                        .setToolbarColor(getResources().getColor(R.color.indigo_400))
                        .setCloseButtonIcon(icon)
                        .build();

                CustomTabActivityHelper.openCustomTab(NutritionProductFragment.this.getActivity(), customTabsIntent, nutritionScoreUri, new WebViewFallback());
                }
            });
        }

        lv.setAdapter(new NutrientLevelListAdapter(getContext(), levelItem));

        if (TextUtils.isEmpty(product.getServingSize())) {
            serving.setVisibility(View.GONE);
        } else {
            serving.append(bold(getString(R.string.txtServingSize)));
            serving.append(" ");
            serving.append(product.getServingSize());
        }

        Nutriments nutriments = product.getNutriments();
        if (isEmpty(nutriments.getCarbonFootprint100g())) {
            carbonFootprint.setVisibility(View.GONE);
        } else {
            carbonFootprint.append(bold(getString(R.string.textCarbonFootprint)));
            carbonFootprint.append(nutriments.getCarbonFootprint100g());
            carbonFootprint.append(nutriments.getCarbonFootprintUnit());
        }
    }

    private int getImageGrade(String grade) {
        int drawable;

        if (grade == null) {
            return R.drawable.ic_error;
        }

        switch (grade.toLowerCase()) {
            case "a":
                drawable = R.drawable.nnc_a;
                break;
            case "b":
                drawable = R.drawable.nnc_b;
                break;
            case "c":
                drawable = R.drawable.nnc_c;
                break;
            case "d":
                drawable = R.drawable.nnc_d;
                break;
            case "e":
                drawable = R.drawable.nnc_e;
                break;
            default:
                drawable = R.drawable.ic_error;
                break;
        }

        return drawable;
    }

    private int getImageLevel(String nutrient) {
        int drawable;

        if (nutrient == null) {
            return R.drawable.ic_error;
        }

        switch (nutrient.toLowerCase()) {
            case "moderate":
                drawable = R.drawable.ic_circle_yellow;
                break;
            case "low":
                drawable = R.drawable.ic_circle_green;
                break;
            case "high":
                drawable = R.drawable.ic_circle_red;
                break;
            default:
                drawable = R.drawable.ic_error;
                break;
        }

        return drawable;
    }

    /**
     *
     * @param nutritionAmount Either "low", "moderate" or "high"
     * @return The localised word for the nutrition amount. If nutritionAmount is neither low,
     * moderate nor high, return nutritionAmount
     */
    private String localiseNutritionLevel(String nutritionAmount){
        if (nutritionAmount == null) {
            return getString(R.string.txt_nutrition_not_found);
        }

        switch (nutritionAmount){
            case "low":
                return getString(R.string.txtNutritionLevelLow);
            case "moderate":
                return getString(R.string.txtNutritionLevelModerate);
            case "high":
                return getString(R.string.txtNutritionLevelHigh);
            default:
                return nutritionAmount;
        }
    }

    @Override
    public void onCustomTabsConnected() {
        img.setClickable(true);
    }

    @Override
    public void onCustomTabsDisconnected() {
        img.setClickable(false);
    }
}
